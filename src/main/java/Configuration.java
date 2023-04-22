import java.util.concurrent.ConcurrentLinkedDeque;

public class Configuration {  //WebsiteData.Data class
    //translation data
    public static final String TRANSLATION_URI = "https://google-translator9.p.rapidapi.com/v2";
    public static final String TRANSLATION_API_KEY = "8be2472a36msh8f684a5c19a2e7fp1efedbjsn205de06bd3c9";
    public static final String TRANSLATION_API_HOST = "google-translator9.p.rapidapi.com";
    public static final boolean doIHaveATranslationApiKey = true;

    private static int maxCrawlDepth;

    public static final int CLIENT_TIMEOUT_IN_SECONDS = 3;
    public static final int MAX_TRIES = 3;
    public static final boolean SLOW_MODE = true;


    private static ConcurrentLinkedDeque<String> urlList = new ConcurrentLinkedDeque<>();
    private static ConcurrentLinkedDeque<String> errorUrls = new ConcurrentLinkedDeque<>();


    //TODO delete debug feature
    public static int successes = 0;
    public static int failures = 0;

    /**
     * Getter and Setter
     */
    public static int getMaxCrawlDepth() {
        return maxCrawlDepth;
    }

    public static void setMaxCrawlDepth(int maxCrawlDepth) {
        Configuration.maxCrawlDepth = maxCrawlDepth;
    }


    public static ConcurrentLinkedDeque<String> getUrlList() {
        return urlList;
    }

    public static void setUrlList(ConcurrentLinkedDeque<String> urlList) {
        Configuration.urlList = urlList;
    }

    public static ConcurrentLinkedDeque<String> getErrorUrls() {
        return errorUrls;
    }

    public static void setErrorUrls(ConcurrentLinkedDeque<String> errorUrls) {
        Configuration.errorUrls = errorUrls;
    }

}
