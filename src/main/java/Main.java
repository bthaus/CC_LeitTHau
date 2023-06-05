import java.util.LinkedList;

public class Main {
    public static WebNode root;
    public static MarkdownFactory markdownFactory;


    public static void main(String[] args) {
        Configuration.setMaxCrawlDepth(Integer.parseInt(args[1]));
        Configuration.setRootUrl(args[0]);

       LinkedList<String> urls=new LinkedList<>();
       LinkedList<WebNode> nodes=new LinkedList<>();

       urls.push("https://www.gmx.at");
       urls.push("https://www.wikipedia.at");
       urls.push("https://www.google.at");

        for (String url:urls) {
            WebNode node=new WebNode(url,Configuration.getMaxCrawlDepth());
            node.startNonBlocking(new Callback() {
                @Override
                public void onComplete() {
                    Translator.createAndStartTranslator(node, args[2],new Callback(){
                        @Override
                        public void onComplete() {
                            markdownFactory.createMarkdownFile(node);
                        }

                        @Override
                        public void onError(Exception e) {
                            markdownFactory.createMarkdownFile(node,e.getMessage());
                        }
                    });
                }

                @Override
                public void onError(Exception e) {

                    e.printStackTrace();
                }
            });
            nodes.push(node);

        }

        markdownFactory = new MarkdownFactory();

        System.out.println("waiting for everything to finish");
        Synchronizer.joinAll();
        for (WebNode node:nodes) {
            markdownFactory.createMarkdownFile(node);
        }

    }
}

