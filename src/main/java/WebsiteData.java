import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;

public class WebsiteData {
    public static final int CLIENT_TIMEOUT_IN_SECONDS = 3;
    public static final int MAX_TRIES = 3;
    public static final boolean SLOW_MODE = true;

    private static WebsiteData parent;
    public static int successes = 0;
    public static int failures = 0;


    private String url;
    private String errorMessage;
    private String header;
    private int depth;
    private boolean successful;
    private int tries;
    private WebsiteData child;

    private ConcurrentLinkedDeque<WebsiteData> children = new ConcurrentLinkedDeque<>();
    public static ConcurrentLinkedDeque<String> urlList = new ConcurrentLinkedDeque<>();
    public static ConcurrentLinkedDeque<String> errorUrls = new ConcurrentLinkedDeque<>();
    public static ConcurrentLinkedDeque<CompletableFuture<HttpResponse<String>>> futures = new ConcurrentLinkedDeque<>();

    public WebsiteData(HttpHeaders header, String url, int depth, boolean success) {
        if(header == null){
            this.header = "no header";
        }else{
            this.header = header.toString();
        }

        this.url = url;
        this.depth = depth;
        this.successful = success;

        if(success){
            successes++;
        }else {
            failures++;
        }

        setTries(1);
        if (parent == null) {
            parent = this;
        }

    }

    //auxiliary constructor for mockdata
    public WebsiteData(String header){
        this.header = header;
    }

    public String getMarkdownString(){      //TODO maybe move to separate class
        String markdownStr = "";
        markdownStr = addNStrings(markdownStr,"#", Main.crawlDepth-depth-1).concat(" ");
        markdownStr = addNStrings(markdownStr,"-", Main.crawlDepth-depth);
        if(successful){
            markdownStr = markdownStr.concat("> **" + url + "** <br>\n");
        }else{
            markdownStr = markdownStr.concat("> *" + url + "* <br>\n");
        }

        for (WebsiteData child:children) {
            markdownStr = markdownStr.concat(child.getMarkdownString());
        }
        return markdownStr;
    }

    private String addNStrings(String word, String value, int repetitions){       //todo maybe move to same class as getMarkdownString
        for (int i = 0; i <= repetitions; i++) {
            word = word.concat(value);
        }
        return word;
    }

    public void translateAll(String language){
        this.translate(language);
        for (WebsiteData child:children) {
            child.translateAll(language);
        }
    }

    /** formatted according to example response on deepl.com/docs-api:
      {
          "translations": [
          {
              "detected_source_language": "EN",
                  "text": "Hallo, Welt!"
          }
        ]
      }*/

    public void translate(String language){
        JsonHelper.TranslationRequestBody body=JsonHelper.getTranslationRequestBody(header,"en", language);
        String bodyString=JsonHelper.getJsonString(body);

        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(Main.TRANSLATION_URI)).POST(HttpRequest.BodyPublishers.ofString(bodyString)).headers("content-type","application/json","X-RapidAPI-Key", Main.TRANSLATION_API_KEY,"X-RapidAPI-Host", Main.TRANSLATION_API_HOST).build();
        CompletableFuture<HttpResponse<String>> response = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(CLIENT_TIMEOUT_IN_SECONDS)).build().sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
        futures.offer(response);

        response.thenAcceptAsync((res) -> {
                removeFuture(response);
                System.out.println(res.body());
                String[] translation = res.body().split("translatedText\": \"");
                this.header = translation[1].substring(0, translation[1].lastIndexOf("\""));
        }).join();
    }

    public void waitForAllRequests(){
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

    public void crawl() {
        //guard clauses
        //first recursion base case
        if (depth == 0) {
            return;
        }
        //second recursion base case
        if(tries > MAX_TRIES){
            parent.getChildren().offer(new WebsiteData(null, url, depth, false));
            errorUrls.offer(url);
            return;
        }
        //don't crawl the same page twice
        if ((urlList.contains(url) || errorUrls.contains(url)) && tries == 1) {
            return;
        }
        //saving already crawled urls
        urlList.offer(url);

        //creating request
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url));
        HttpRequest req = builder.GET().build();
        CompletableFuture<HttpResponse<String>> response = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(CLIENT_TIMEOUT_IN_SECONDS)).build().sendAsync(req, HttpResponse.BodyHandlers.ofString());     //TODO remember last lecture, maybe make it less nested..

        // saving away all future-objects for synchronization
        futures.offer(response);

        //asynchrounusly handle response
        response.thenAcceptAsync((res) -> {

            //creating website data instance
            WebsiteData fetched = new WebsiteData(res.headers(), url, depth, true);

            //placing data as child in parent node
            parent.getChildren().offer(fetched);

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
                    crawl();
                    child = new WebsiteData(null, link, getDepth()-1,true);
                    child.crawl();
                }
            }
            //on exception call this
        }).exceptionally((exc) -> {
            //removing request from active requests as its done
            removeFuture(response);

            //recursively calling crawl
            setTries(getTries()+1);
            crawl();
            return null;
        });

        //to balance traffic weight only leaf nodes are called asynchronously
        if(depth <= 2 && SLOW_MODE){
            response.join();
        }
    }

    public void killAllFutures() {
        //cancelling all requests that fell under unexpected error cases
        for (CompletableFuture<HttpResponse<String>> future : futures) {
            future.cancel(true);
        }
    }

    //auxiliary method
    public void removeFuture(CompletableFuture<HttpResponse<String>> f) {
        futures.remove(f);
    }
    public void print(){
        System.out.println("address: "+ url +" depth: "+depth+" headers: "+header+" error message: "+errorMessage);
    }

    /**
     * Getter and Setter methods TODO remove methods not needed from outside -
     */
    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getTries() {
        return tries;
    }

    public void setTries(int tries) {
        this.tries = tries;
    }
    public WebsiteData getParent() {
        return parent;
    }

    public void setParent(WebsiteData parent) {
        this.parent = parent;
    }

    public ConcurrentLinkedDeque<WebsiteData> getChildren() {
        return children;
    }

    public void setChildren(ConcurrentLinkedDeque<WebsiteData> children) {
        this.children = children;
    }
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public WebsiteData getChild() {
        return child;
    }

    public void setChild(WebsiteData child) {
        this.child = child;
    }
}
