import com.google.api.services.translate.Translate;
import com.google.cloud.translate.Language;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class main {
    static ConcurrentLinkedDeque<String> urlList = new ConcurrentLinkedDeque<>();
    static ConcurrentLinkedDeque<String> errorUrls = new ConcurrentLinkedDeque<>();
    static ConcurrentLinkedDeque<CompletableFuture<HttpResponse<String>>> futures = new ConcurrentLinkedDeque<>();
    static WebsiteData root;

    static Scanner in = new Scanner(System.in);
   public static final int CRAWL_DEPTH = 3;
    static final int CLIENT_TIMEOUT_IN_SECONDS = 3;
    static final int MAX_TRIES=3;
    static final boolean SLOW_MODE=true;

    public static void helloWorld(String msg) {

    }

    public static void main(String[] args) {
        root=new WebsiteData(null,args[0],-1,false);
        crawl(args[0], CRAWL_DEPTH,root,1);
        waitForAllRequests();
        translateEverything(args[1]);
        createMarkdownFile();





    }

    private static void translateEverything(String german) {

    }

    public static void createMarkdownFile(){

        System.out.println(WebsiteData.successes+" successes");
        System.out.println(WebsiteData.failures+" failures");
        Path path
                = Paths.get("C:\\Users\\bwues\\Desktop\\U_SS_23\\Clean_Code\\Project\\src\\main\\markdown.md");

        // Custom string as an input

        // Try block to check for exceptions
        try {
            // Now calling Files.writeString() method
            // with path , content & standard charsets
            Files.writeString(path, root.children.pop().getMarkdownString(),
                    StandardCharsets.UTF_8);
        }

        // Catch block to handle the exception
        catch (IOException ex) {
            // Print messqage exception occurred as
            // invalid. directory local path is passed
            System.out.print("Invalid Path");
        }

    }



    public static void waitForAllRequests(){
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
                if (keepAlive > CLIENT_TIMEOUT_IN_SECONDS) {
                    System.out.println("remaining requests cancelled due to timeout");
                    killAllFutures();
                }
                System.out.println(futures.size() + " crawlers running" + urlList.size() + " websites pinged and " + errorUrls.size() + " websites unsuccessfully pinged");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        killAllFutures();
    }

    public static void crawl(String url, int depth,WebsiteData parent,int tries) {

        if (depth == 0) {
            return;
        }
        if(tries>MAX_TRIES){
            parent.children.offer(new WebsiteData(null, url, depth, false));
            errorUrls.offer(url);
            return;
        }

        if ((urlList.contains(url)||errorUrls.contains(url))&&tries==1) {
            return;}

        urlList.offer(url);
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url));
        HttpRequest req = builder.GET().build();
        CompletableFuture<HttpResponse<String>> response = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(CLIENT_TIMEOUT_IN_SECONDS)).build().sendAsync(req, HttpResponse.BodyHandlers.ofString());

        futures.offer(response);
        response.thenAcceptAsync((res) -> {

            WebsiteData fetched=new WebsiteData(res.headers(), url, depth, true);
            parent.children.offer(fetched);
            removeFuture(response);
            String hrefs[] = res.body().split("href=\"");
            urlList.offer(url);
            for (String a : hrefs
            ) {

                String link;
                try {
                    link = a.substring(0, a.indexOf("\""));
                } catch (Exception e) {
                    link = a;
                }
                if (link.contains("https://") || link.contains("http://")) {
                    crawl(link, depth - 1,fetched,tries);



                }

            }

        }).exceptionally((exc) -> {

              removeFuture(response);
              crawl(url,depth,parent,tries+1);


            return null;
        });

        if(depth==1&&SLOW_MODE)response.join();

    }

    static void killAllFutures() {
        for (CompletableFuture<HttpResponse<String>> future : futures
        ) {
            future.cancel(true);
        }
    }

    static void removeFuture(CompletableFuture<HttpResponse<String>> f) {
        futures.remove(f);
    }
}

