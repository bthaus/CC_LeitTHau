public class Main {

    public static WebsiteData root;
    public static MarkdownFactory markdownFactory;
    public static Translator translator;

    public static void main(String[] args) {
        config.setCrawlDepth(Integer.parseInt(args[1]));

        root = new WebsiteData(null, args[0], config.getCrawlDepth(),true);
        markdownFactory = new MarkdownFactory();
        translator = new Translator(args[2]);

        root.crawl();
        root.waitForAllRequests();

        translator.deepTranslate(root);
        root.waitForAllRequests();

        markdownFactory.createMarkdownFile(root);
    }
}

/** todo things cc still got to do
 * test cases
 * read.me file
 */

