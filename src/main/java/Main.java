public class Main {

    public static WebsiteData root;
    public static MarkdownFile markdownFile;

    public static void main(String[] args) {
        Data.setCrawlDepth(Integer.parseInt(args[1]));
        root = new WebsiteData(null, args[0], Data.getCrawlDepth(),true);
        markdownFile = new MarkdownFile();

        root.crawl();
        root.waitForAllRequests();

        //translateEverything(args[2]);
        //root.waitForAllRequests();

        markdownFile.createMarkdownFile(Data.getChildren());
    }

    private static void translateEverything(String language) {
        if(Data.doIHaveATranslationApiKey){
            root.translateChildren(language);
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

