public class Main {

    public static Webnode root;
    public static MarkdownFactory markdownFactory;
    public static Translator translator;
    public  static Synchronizer synchronizer;

    public static void main(String[] args) {
        Configuration.setMaxCrawlDepth(Integer.parseInt(args[1]));

        synchronizer = new Synchronizer();
        root = new Webnode(null, args[0], Configuration.getMaxCrawlDepth(),true, synchronizer);
        markdownFactory = new MarkdownFactory();
        translator = new Translator(args[2], synchronizer);

        root.crawl();
        synchronizer.waitForAllRequests();

        translator.deepTranslate(root);
        synchronizer.waitForAllRequests();

        markdownFactory.createMarkdownFile(root);
    }
}

/** todo things cc still got to do
 * test cases
 * read.me file
 */

