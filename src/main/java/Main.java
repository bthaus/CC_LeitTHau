import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.*;

public class Main {

    public static final String TRANSLATION_URI = "https://google-translator9.p.rapidapi.com/v2";
    public static final String TRANSLATION_API_KEY = "8be2472a36msh8f684a5c19a2e7fp1efedbjsn205de06bd3c9";
    public static final String TRANSLATION_API_HOST = "google-translator9.p.rapidapi.com";
    public static final boolean doIHaveATranslationApiKey=true;

    static ConcurrentLinkedDeque<String> urlList = new ConcurrentLinkedDeque<>();
    static ConcurrentLinkedDeque<String> errorUrls = new ConcurrentLinkedDeque<>();
    static ConcurrentLinkedDeque<CompletableFuture<HttpResponse<String>>> futures = new ConcurrentLinkedDeque<>();

    static WebsiteData root;

    public static final int CRAWL_DEPTH = 3;
    static final int CLIENT_TIMEOUT_IN_SECONDS = 3;
    static final int MAX_TRIES=3;
    static final boolean SLOW_MODE=true;


    public static void main(String[] args) {

        root=new WebsiteData(null,args[0],-1,false);
        crawl(args[0], CRAWL_DEPTH, root,1);
        waitForAllRequests();

        translateEverything(args[1]);
        waitForAllRequests();
        createMarkdownFile();

    }

    private static void translateEverything(String language) {
        if(doIHaveATranslationApiKey){
            root.translateAll(language);
        }
    }

    public static void createMarkdownFile(){

        System.out.println(WebsiteData.successes+" successes");
        System.out.println(WebsiteData.failures+" failures");

        Path path = Paths.get("C:\\Users\\claud\\OneDrive\\Anlagen\\Uni\\Master\\1. Semester\\Clean Code\\CC_LeitTHau\\src\\main\\markdown.md");

        try {
            Files.writeString(path, root.children.pop().getMarkdownString(), StandardCharsets.UTF_8);
        }catch (IOException ex) {
            System.out.print("Invalid Path");
        }

    }

    public static void waitForAllRequests(){

        //as this is no operating systems course i handeled joining for threads quite liberally.
        //every second it is checked if no further requests are called
        int keepAlive = 0;
        int temp = 0;
        while (futures.size() > 0) {
            try {
                Thread.sleep(1000);

                if (temp == futures.size()) {
                    keepAlive++;
                } else {
                    keepAlive = 0;
                }

                temp = futures.size();

                //wir haben alle probleme gelöst. aber da sind ja noch welche?? TÖTET SIE ALLE
                //if for timeout seconds no request is added or removed all requests are cancelled
                if (keepAlive > CLIENT_TIMEOUT_IN_SECONDS) {
                    System.out.println("remaining requests cancelled due to timeout");
                    killAllFutures();
                }
                System.out.println(futures.size() + " requests active" );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //technically not neccessary but for peace of mind
        killAllFutures();
    }

    public static void crawl(String url, int depth,WebsiteData parent,int tries) {
        //guard clauses
        //first recursion base case
        if (depth == 0) {
            return;
        }
        //second recursion base case
        if(tries>MAX_TRIES){
            parent.children.offer(new WebsiteData(null, url, depth, false));
            errorUrls.offer(url);
            return;
        }
        //dont crawl the same page twice
        if ((urlList.contains(url)||errorUrls.contains(url))&&tries==1) {
            return;
        }
        //saving already crawled urls
        urlList.offer(url);

        //creating request
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url));
        HttpRequest req = builder.GET().build();
        CompletableFuture<HttpResponse<String>> response = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(CLIENT_TIMEOUT_IN_SECONDS)).build().sendAsync(req, HttpResponse.BodyHandlers.ofString());

        // saving away all future-objects for synchronization
        futures.offer(response);

        //asynchrounusly handle response
        response.thenAcceptAsync((res) -> {

            //creating website data instance
            WebsiteData fetched=new WebsiteData(res.headers(), url, depth, true);

            //placing data as child in parent node
            parent.children.offer(fetched);

            //removing future from active requests
            removeFuture(response);

            //parsing body
            String hrefs[] = res.body().split("href=\"");

            //iterating over all links
            for (String a : hrefs) {
                //parsing link
                String link;
                try {
                    link = a.substring(0, a.indexOf("\""));
                } catch (Exception e) {
                    link = a;
                }

                //checking for validity and recursively calling crawl
                if (link.contains("https://") || link.contains("http://")) {
                    crawl(link, depth-1, fetched, tries);
                }
            }
        //on exception call this
        }).exceptionally((exc) -> {
            //removing request from active requests as its done
            removeFuture(response);

            //recursively calling crawl
            crawl(url, depth, parent,tries+1);

            return null;
        });

        //to balance traffic weight only leaf nodes are called asynchronously
        if(depth<=2&&SLOW_MODE){
            response.join();
        }

    }

    static void killAllFutures() {
        //cancelling all requests that fell under unexpected error cases
        for (CompletableFuture<HttpResponse<String>> future : futures) {
            future.cancel(true);
        }
    }

    //auxiliary method
    static void removeFuture(CompletableFuture<HttpResponse<String>> f) {
        futures.remove(f);
    }
}

