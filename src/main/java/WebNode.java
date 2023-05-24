import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;

@Getter
@Setter
public class WebNode {
    private final String url;
    private String header = "I am a leaf and hence have no header";
    private final int depth;
    private boolean successful = true;
    private int tries;

    private static final LinkedList<WebNode> rootNodes = new LinkedList<>();

    private Synchronizer synchronizer = new Synchronizer();
    private Thread rootThread;
    private Callback callback;
    private final Task task;


    private final ConcurrentLinkedDeque<WebNode> childrenNodes = new ConcurrentLinkedDeque<>();
    public final static ConcurrentLinkedDeque<String> urlList = new ConcurrentLinkedDeque<>();
    public final static ConcurrentLinkedDeque<String> errorUrls = new ConcurrentLinkedDeque<>();


    public WebNode(String url, int depth) {
        this.url = url;
        this.depth = depth;

        tries = 1;
        synchronizer.setIntervalMessage(" origin: " + url);
        this.task = this::crawl;

    }


    public void startNonBlocking(Callback callback) {
        this.callback = callback;
        rootThread = synchronizer.createBlockedTask(task, callback);
        rootNodes.push(this);
        rootThread.start();
    }

    public void crawl() {
        if (isBaseCase()) return;

        //creating request
        CompletableFuture<HttpResponse<String>> response = createRequest();

        //saving already crawled urls
        urlList.offer(url);

        // saving away all future-objects for synchronization
        synchronizer.offerFuture(response);


        //asynchronously handle response
        response.thenAcceptAsync((res) -> {
            setHeader(res.headers());
            //removing future from active requests
            synchronizer.removeFuture(response);

            //parsing body
            String[] hrefs = res.body().split("href=\"");

            //iterating over all links
            for (String link : hrefs) {
                //parsing link
                String linkSnipped;
                try {
                    linkSnipped = link.substring(0, link.indexOf("\""));
                } catch (Exception e) {
                    linkSnipped = link;
                }

                //checking for validity and recursively calling crawl
                if (linkSnipped.contains("https://") || linkSnipped.contains("http://")) {
                    WebNode child = new WebNode(linkSnipped, getDepth() - 1);
                    childrenNodes.offer(child);
                    child.synchronizer = this.synchronizer;
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
        if (depth <= 1 && Configuration.SLOW_MODE) {
            response.join();
        }
    }

    public CompletableFuture<HttpResponse<String>> createRequest() {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url));
        HttpRequest req = builder.GET().build();
        return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(Configuration.CLIENT_TIMEOUT_IN_SECONDS)).build().sendAsync(req, HttpResponse.BodyHandlers.ofString());
    }

    public boolean isBaseCase() {
        //guard clauses
        //first recursion base case
        if (depth == 0) {
            return true;
        }
        //second recursion base case
        if (tries > Configuration.MAX_TRIES) {
            errorUrls.offer(url);
            successful = false;
            return true;
        }
        //don't crawl the same page twice. if urllist contains the url it must be try 2, otherwise it would be a recall of the same url.
        // so if the first appearance of a link failed it has to be inside errorulrs to be returned, which it only is if it has been crawled 3 times
        return (urlList.contains(url) && tries == 1) || (errorUrls.contains(url));
    }

    public void setHeader(HttpHeaders header) {
        if (header == null) {
            this.header = "no header";
        } else {
            this.header = header.toString();
        }
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getName() {
        return url.substring(url.indexOf("www."));
    }
}
