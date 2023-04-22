import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;

public class WebsiteData {
    private static WebsiteData parent;

    private String url;
    private String errorMessage;
    private String header;
    private int depth;
    private boolean successful;
    private int tries;
    private  ConcurrentLinkedDeque<WebsiteData> children = new ConcurrentLinkedDeque<>();

    public WebsiteData(HttpHeaders header, String url, int depth, boolean success) {
        checkObjectStatus(header, success);

        this.url = url;
        this.depth = depth;
        this.successful = success;

        setTries(1);
    }

    public void checkObjectStatus (HttpHeaders header, boolean success){
        if(header == null){
            this.header = "no header";
        }else{
            this.header = header.toString();
        }

        if (parent == null) {
            parent = this;
        }
    }

    //auxiliary constructor for mockdata
    public WebsiteData(String header){
        this.header = header;
    }



    public void waitForAllRequests(){
        //as this is no operating systems course i handeled joining for threads quite liberally.
        //every second it is checked if no further requests are called
        int keepAlive = 0;
        int temp = 0;
        while (config.getFutures().size() > 0) {
            try {
                Thread.sleep(1000);

                if (temp == config.getFutures().size()) {
                    keepAlive++;
                } else {
                    keepAlive = 0;
                }

                temp = config.getFutures().size();

                //if for timeout seconds no request is added or removed all requests are cancelled
                if (keepAlive > config.CLIENT_TIMEOUT_IN_SECONDS) {
                    System.out.println("remaining requests cancelled due to timeout");
                    config.killAllFutures();
                    break;
                }
                System.out.println(config.getFutures().size() + " requests active" );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //technically not neccessary but for peace of mind
        config.killAllFutures();
    }

    public void crawl() {
        //guard clauses
        //first recursion base case
        if (depth == 0) {
            return;
        }
        //second recursion base case
        if(tries > config.MAX_TRIES){
           children.offer(new WebsiteData(null, url, depth, false));
            config.getErrorUrls().offer(url);
            return;
        }
        //don't crawl the same page twice
        if ((config.getUrlList().contains(url) || (config.getErrorUrls().contains(url)) && tries == 1)) {
            return;
        }
        //saving already crawled urls
        config.getUrlList().offer(url);

        //creating request
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url));
        HttpRequest req = builder.GET().build();
        CompletableFuture<HttpResponse<String>> response = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(config.CLIENT_TIMEOUT_IN_SECONDS)).build().sendAsync(req, HttpResponse.BodyHandlers.ofString());     //TODO remember last lecture, maybe make it less nested..

        // saving away all future-objects for synchronization
        config.getFutures().offer(response);

        //asynchrounusly handle response
        response.thenAcceptAsync((res) -> {

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
                    WebsiteData child = new WebsiteData(res.headers(), link, getDepth()-1,true);
                    children.offer(child);
                    child.crawl();

                }
            }
            //on exception call this
        }).exceptionally((exception) -> {

            //removing request from active requests as its done
            removeFuture(response);

            //recursively calling crawl
            tries++;
            crawl();
            return null;
        });

        //to balance traffic weight only leaf nodes are called asynchronously
        if(depth <= 1 && config.SLOW_MODE){
            response.join();
        }
    }

    //auxiliary method
    public void removeFuture(CompletableFuture<HttpResponse<String>> f) {
        config.getFutures().remove(f);
    }
    public void print(){
        System.out.println("address: "+ url +" depth: "+depth+" headers: "+header+" error message: "+errorMessage);
    }




    /**
     * Getter and Setter methods TODO remove methods not needed from outside -
     */
    public static WebsiteData getParent() {
        return parent;
    }

    public static void setParent(WebsiteData parent) {
        WebsiteData.parent = parent;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public int getTries() {
        return tries;
    }

    public void setTries(int tries) {
        this.tries = tries;
    }

    public ConcurrentLinkedDeque<WebsiteData> getChildren() {
        return children;
    }


}
