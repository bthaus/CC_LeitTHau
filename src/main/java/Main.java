public class Main {

    public static WebNode root;
    public static MarkdownFactory markdownFactory;
    public static Translator translator;


    public static void main(String[] args) {
        Configuration.setMaxCrawlDepth(Integer.parseInt(args[1]));


        root = new WebNode( args[0], Configuration.getMaxCrawlDepth(),true);
        markdownFactory = new MarkdownFactory();
        translator = new Translator(args[2]);

        root.crawl();
        root.waitForRequests();
        System.out.println(WebNode.counter);

        translator.deepTranslate(root);
        translator.waitForRequests();

        markdownFactory.createMarkdownFile(root);
    }
}

/** todo things cc still got to do
 * test cases
 * read.me file
 */

