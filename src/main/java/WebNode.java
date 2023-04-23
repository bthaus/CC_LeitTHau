import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;


public class WebNode implements Syncer {
    private String url;
    private String header = "I am a leaf and hence have no header";
    private int depth;
    private boolean successful;
    private int tries;

    private final ConcurrentLinkedDeque<WebNode> childrenNodes = new ConcurrentLinkedDeque<>();
    private final static ConcurrentLinkedDeque<String> urlList = new ConcurrentLinkedDeque<>();
    private final static ConcurrentLinkedDeque<String> errorUrls = new ConcurrentLinkedDeque<>();

    public WebNode( String url, int depth, boolean success)  {
        this.url = url;
        this.depth = depth;
        this.successful = success;
        setTries(1);
    }

    //auxiliary constructor for mockdata
    public WebNode(String header){
        this.header = header;
    }


    public void crawl() {
        if (checkBaseCases()){
            return;
        }

        //saving already crawled urls
        urlList.offer(url);

        //creating request
        CompletableFuture<HttpResponse<String>> response = createRequest();

        // saving away all future-objects for synchronization
        offerFuture(response);

        //asynchronously handle response
        response.thenAcceptAsync((res) -> {
            setHeader(res.headers());
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
                    WebNode child = new WebNode( link, getDepth()-1, true);
                    childrenNodes.offer(child);
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
        if(depth <= 1 && Configuration.SLOW_MODE){
            response.join();
        }
    }

    public CompletableFuture<HttpResponse<String>> createRequest(){
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url));
        HttpRequest req = builder.GET().build();
        return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(Configuration.CLIENT_TIMEOUT_IN_SECONDS)).build().sendAsync(req, HttpResponse.BodyHandlers.ofString());
    }

    public boolean checkBaseCases(){
        //guard clauses
        //first recursion base case
        if (depth == 0) {
            return true;
        }
        //second recursion base case
        if(tries > Configuration.MAX_TRIES){
            childrenNodes.offer(new WebNode(url, depth, false));
            urlList.offer(url);
            return true;
        }
        //don't crawl the same page twice. if urllist contains the url it must be try 2, otherwise it would be a recall of the same url. so if the first appearance of a link failed it has to be inside errorulrs to be returned, which it only is if it has been crawled 3 times
        if ((urlList.contains(url) && tries == 1) || (errorUrls.contains(url))) {
            return true;
        }
        return false;
    }



    /**
     * Getter and Setter methods TODO remove methods not needed from outside -
     */
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(HttpHeaders header) {
        if(header == null){
            this.header = "no header";
        }else{
            this.header = header.toString();
        }
    }
    public void setHeader(String header){
        this.header=header;
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
