public class Main {
    public static WebNode root;
    public static MarkdownFactory markdownFactory;
    public static Translator translator;

    public static void main (String[] args) {
        Configuration.setMaxCrawlDepth(Integer.parseInt(args[1]));
        Configuration.setRootUrl(args[0]);

        root = new WebNode(Configuration.getRootUrl(), Configuration.getMaxCrawlDepth(),true);
        markdownFactory = new MarkdownFactory();
        translator = new Translator(args[2]);

        root.crawl();
        root.waitForRequests();

        translator.deepTranslate(root);
        translator.waitForRequests();

        markdownFactory.createMarkdownFile(root);
    }
}

