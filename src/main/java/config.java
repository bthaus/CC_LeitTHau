import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;

public class config {  //WebsiteData.Data class
    //translation data
    public static final String TRANSLATION_URI = "https://google-translator9.p.rapidapi.com/v2";
    public static final String TRANSLATION_API_KEY = "8be2472a36msh8f684a5c19a2e7fp1efedbjsn205de06bd3c9";
    public static final String TRANSLATION_API_HOST = "google-translator9.p.rapidapi.com";
    public static final boolean doIHaveATranslationApiKey = true;

    private static int crawlDepth;    //TODO unsure if i should leave it here or move it back to webcrawler
    // i changed to user input cause in the assignment description firstly it says it should be user input, later it says _you might allow the user to configure this_ sooo...

    public static final int CLIENT_TIMEOUT_IN_SECONDS = 3;
    public static final int MAX_TRIES = 3;
    public static final boolean SLOW_MODE = true;


    private static ConcurrentLinkedDeque<String> urlList = new ConcurrentLinkedDeque<>();
    private static ConcurrentLinkedDeque<String> errorUrls = new ConcurrentLinkedDeque<>();
    private static ConcurrentLinkedDeque<CompletableFuture<HttpResponse<String>>> futures = new ConcurrentLinkedDeque<>();


    //TODO delete debug feature
    public static int successes = 0;
    public static int failures = 0;

    /**
     * Getter and Setter
     */
    public static int getCrawlDepth() {
        return crawlDepth;
    }

    public static void setCrawlDepth(int crawlDepth) {
        config.crawlDepth = crawlDepth;
    }


    public static ConcurrentLinkedDeque<String> getUrlList() {
        return urlList;
    }

    public static void setUrlList(ConcurrentLinkedDeque<String> urlList) {
        config.urlList = urlList;
    }

    public static ConcurrentLinkedDeque<String> getErrorUrls() {
        return errorUrls;
    }

    public static void setErrorUrls(ConcurrentLinkedDeque<String> errorUrls) {
        config.errorUrls = errorUrls;
    }

    public static ConcurrentLinkedDeque<CompletableFuture<HttpResponse<String>>> getFutures() {
        return futures;
    }

    public static void setFutures(ConcurrentLinkedDeque<CompletableFuture<HttpResponse<String>>> futures) {
        config.futures = futures;
    }

    public static void killAllFutures() {
        //cancelling all requests that fell under unexpected error cases
        while (futures.size() > 0) {
            futures.pop().cancel(true);
        }

    }
}
