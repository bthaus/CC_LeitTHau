import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static final String TRANSLATION_URI = "https://google-translator9.p.rapidapi.com/v2";
    public static final String TRANSLATION_API_KEY = "8be2472a36msh8f684a5c19a2e7fp1efedbjsn205de06bd3c9";
    public static final String TRANSLATION_API_HOST = "google-translator9.p.rapidapi.com";
    public static final boolean doIHaveATranslationApiKey=true;

    private static WebsiteData root;
    public static int crawlDepth;       //TODO i changed to user input cause in the assignment description firstly it says it should be user input, later it says _you might allow the user to configure this_ sooo...


    public static void main(String[] args) {
        crawlDepth = Integer.parseInt(args[1]);
        root = new WebsiteData(null, args[0], crawlDepth,true);

        root.crawl();
        root.waitForAllRequests();

        translateEverything(args[2]);
        root.waitForAllRequests();
        createMarkdownFile();
    }

    private static void translateEverything(String language) {
        if(doIHaveATranslationApiKey){
            root.translateAll(language);
        }
    }

    public static void createMarkdownFile(){
        System.out.println(WebsiteData.successes+" successes");
        System.out.println(WebsiteData.failures+" failures");

        Path path = Paths.get("C:\\Users\\claud\\OneDrive\\Anlagen\\Uni\\Master\\1. Semester\\Clean Code\\assignment_1\\CC_LeitTHau\\src\\main\\markdown.md");

        try {
            Files.writeString(path, root.getMarkdownString(), StandardCharsets.UTF_8);
        }catch (IOException ex) {
            System.out.print("Invalid Path");
        }
    }
}

/** TODO ?? - things cc want bodos opinion on
 * move markdownfile methods in own class?
 * translation in seperate class? or to markdownfile class?
 * comments (personally i think they are pretty good cause they helped me a lot, but maybe there are too many of them? unsure)
 *
 * todo things cc still got to do
 * test cases
 * read.me file
 */

