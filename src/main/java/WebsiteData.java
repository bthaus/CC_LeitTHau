import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class WebsiteData {
    private static WebsiteData parent;

    private String url;
    private String errorMessage;
    private String header;
    private int depth;
    private boolean successful;
    private int tries;

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
        while (Data.getFutures().size() > 0) {
            try {
                Thread.sleep(1000);

                if (temp == Data.getFutures().size()) {
                    keepAlive++;
                } else {
                    keepAlive = 0;
                }

                temp = Data.getFutures().size();

                //if for timeout seconds no request is added or removed all requests are cancelled
                if (keepAlive > Data.CLIENT_TIMEOUT_IN_SECONDS) {
                    System.out.println("remaining requests cancelled due to timeout");
                    killAllFutures();
                }
                System.out.println(Data.getFutures().size() + " requests active" );
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
        if(tries > Data.MAX_TRIES){
            Data.getChildren().offer(new WebsiteData(null, url, depth, false));
            Data.getErrorUrls().offer(url);
            return;
        }
        //don't crawl the same page twice
        if ((Data.getUrlList().contains(url) || Data.getErrorUrls().contains(url)) && tries == 1) {
            return;
        }
        //saving already crawled urls
        Data.getUrlList().offer(url);

        //creating request
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url));
        HttpRequest req = builder.GET().build();
        CompletableFuture<HttpResponse<String>> response = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(Data.CLIENT_TIMEOUT_IN_SECONDS)).build().sendAsync(req, HttpResponse.BodyHandlers.ofString());     //TODO remember last lecture, maybe make it less nested..

        // saving away all future-objects for synchronization
        Data.getFutures().offer(response);

        //asynchrounusly handle response
        response.thenAcceptAsync((res) -> {

            //creating website data instance
            WebsiteData fetched = new WebsiteData(res.headers(), url, depth, true);

            //placing data as child in parent node
            Data.getChildren().offer(fetched);

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
                    WebsiteData child = new WebsiteData(null, link, getDepth()-1,true);
                    child.crawl();
                }
            }
            //on exception call this
        }).exceptionally((exc) -> {
            //removing request from active requests as its done
            removeFuture(response);

            //recursively calling crawl
            tries++;
            crawl();
            return null;
        });

        //to balance traffic weight only leaf nodes are called asynchronously
        if(depth <= 2 && Data.SLOW_MODE){
            response.join();
        }
    }

    public void killAllFutures() {
        //cancelling all requests that fell under unexpected error cases
        for (CompletableFuture<HttpResponse<String>> future : Data.getFutures()) {
            future.cancel(true);
        }
    }

    //auxiliary method
    public void removeFuture(CompletableFuture<HttpResponse<String>> f) {
        Data.getFutures().remove(f);
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


}
