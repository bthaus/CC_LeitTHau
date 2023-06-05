import java.util.LinkedList;

public class Main {
    public static MarkdownFactory markdownFactory;


    public static void main(String[] args) {
        Configuration.setMaxCrawlDepth(Integer.parseInt(args[1]));

        LinkedList<String> urls = new LinkedList<>();
        LinkedList<WebNode> nodes = new LinkedList<>();

        urls.push("https://www.facebook.at");
        urls.push("https://www.wikipedia.at");
        urls.push("https://www.google.at");

        for (String url : urls) {
            WebNode node = new WebNode(url, Configuration.getMaxCrawlDepth());
            node.startNonBlocking(new Callback() {
                @Override
                public void onComplete(Object o) {
                    WebNodeTranslator translator = new WebNodeTranslator(args[2], node);
                    translator.startNonBlocking(new Callback() {

                        @Override
                        public void onComplete(Object o) {

                            markdownFactory.createMarkdownFile(node);

                        }

                        @Override
                        public void onError(Exception e) {

                            markdownFactory.createMarkdownFile(node, e.getMessage());
                        }
                    });

                }


                @Override
                public void onError(Exception e) {
                    Log.stackTrace(e);
                }
            });
            nodes.push(node);

        }

        markdownFactory = new MarkdownFactory();

        Log.info("waiting for everything to finish");
        Synchronizer.joinAll();
        for (WebNode node : nodes) {
            markdownFactory.createMarkdownFile(node);
        }

    }
}

