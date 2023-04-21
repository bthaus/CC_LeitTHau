public class Main {

    public static WebsiteData root;
    public static MarkdownFile markdownFile;
    public static Translator translator;

    public static void main(String[] args) {
        Data.setCrawlDepth(Integer.parseInt(args[1]));

        root = new WebsiteData(null, args[0], Data.getCrawlDepth(),true);
        markdownFile = new MarkdownFile();
        translator = new Translator();

        root.crawl();
        root.waitForAllRequests();

        translator.translateChildren(args[2]);
        root.waitForAllRequests();

        markdownFile.createMarkdownFile(Data.getChildren());
    }
}

/** todo things cc still got to do
 * test cases
 * read.me file
 */

