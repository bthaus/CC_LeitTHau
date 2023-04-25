import java.util.concurrent.ConcurrentLinkedDeque;

public class Configuration {
    //translation data
    public static final String TRANSLATION_URI = "https://google-translator9.p.rapidapi.com/v2";
    public static final String TRANSLATION_API_KEY = "8be2472a36msh8f684a5c19a2e7fp1efedbjsn205de06bd3c9";
    public static final String TRANSLATION_API_HOST = "google-translator9.p.rapidapi.com";
    public static final boolean doIHaveATranslationApiKey = true;

    private static String rootUrl;
    private static int maxCrawlDepth;

    public static final int CLIENT_TIMEOUT_IN_SECONDS = 3;
    public static final int MAX_TRIES = 3;
    public static final boolean SLOW_MODE = true;
    public static final int LOWEST_FATAL_STATUS_CODE = 299;


    //for debugging and checks
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

    public static String getRootUrl() {
        return rootUrl;
    }

    public static void setRootUrl(String rootUrl) {
        Configuration.rootUrl = rootUrl;
    }
}
