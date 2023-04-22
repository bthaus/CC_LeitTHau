import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;


public class WebNode {
    private static WebNode parent;

    private String url;
    private String header;
    private int depth;
    private boolean successful;
    private int tries;
    private Synchronizer synchronizer;
    private final ConcurrentLinkedDeque<WebNode> childrenNodes = new ConcurrentLinkedDeque<>();

    public WebNode(HttpHeaders header, String url, int depth, boolean success, Synchronizer synchronizer) {
        checkObjectStatus(header);

        this.url = url;
        this.depth = depth;
        this.successful = success;

        setTries(1);
        this.synchronizer = synchronizer;
    }

    public void checkObjectStatus (HttpHeaders header){
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
    public WebNode(String header){
        this.header = header;
    }

    public void crawl() {
        //guard clauses
        //first recursion base case
        if (depth == 0) {
            return;
        }
        //second recursion base case
        if(tries > Configuration.MAX_TRIES){
           childrenNodes.offer(new WebNode(null, url, depth, false, synchronizer));
            offerErrorUrl(url);
            return;
        }
        //don't crawl the same page twice
        if ((containsErrorUrl(url) || (containsErrorUrl(url)) && tries == 1)) {
            return;
        }
        //saving already crawled urls
        offerErrorUrl(url);

        //creating request
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url));
        HttpRequest req = builder.GET().build();
        CompletableFuture<HttpResponse<String>> response = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(Configuration.CLIENT_TIMEOUT_IN_SECONDS)).build().sendAsync(req, HttpResponse.BodyHandlers.ofString());     //TODO remember last lecture, maybe make it less nested..

        // saving away all future-objects for synchronization
        synchronizer.offerFuture(response);

        //asynchronously handle response
        response.thenAcceptAsync((res) -> {

            //removing future from active requests
            synchronizer.removeFuture(response);

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
                    WebNode child = new WebNode(res.headers(), link, getDepth()-1, true, synchronizer);
                    childrenNodes.offer(child);
                    child.crawl();
                }
            }
            //on exception call this
        }).exceptionally((exception) -> {

            //removing request from active requests as its done
            synchronizer.removeFuture(response);

            //recursively calling crawl
            tries++;
            crawl();
            return null;
        });

        //to balance traffic weight only leaf nodes are called asynchronously
        if(depth <= 1 && Configuration.SLOW_MODE){
            response.join();
        }
    }

    public void offerErrorUrl(String url){
        Configuration.getUrlList().offer(url);
    }
    public boolean containsErrorUrl(String url){
        return Configuration.getUrlList().contains(url);
    }


    /**
     * Getter and Setter methods TODO remove methods not needed from outside -
     */
    public static WebNode getParent() {
        return parent;
    }

    public static void setParent(WebNode parent) {
        WebNode.parent = parent;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public ConcurrentLinkedDeque<WebNode> getChildrenNodes() {
        return childrenNodes;
    }


}
