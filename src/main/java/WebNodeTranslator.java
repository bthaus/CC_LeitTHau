public class WebNodeTranslator extends Translator{


    WebNode webNode;

    WebNodeTranslator(String language, WebNode webNode){
        super(language);
        this.webNode=webNode;
        getSynchronizer().setIntervalMessage(webNode.getUrl() + "'s nodes are being translated");
    }


    public void startNonBlocking(Callback callback) {
        getSynchronizer().createNonBlockingTask(() -> deepTranslate(webNode), callback);
        getSynchronizer().startTask();
    }

    public void deepTranslate(WebNode node) {
        if (checkForTranslationApiKey()) {

            Log.debug("starting translation");
            translateNonBlocking(node.getHeader(), new Callback() {
            @Override
            public void onComplete(Object o) {
                node.setHeader(o.toString());
            }

            @Override
            public void onError(Exception e) {
                Log.debug("deeptranslate error");
                getSynchronizer().onError(e);
            }
        });
            Log.debug("starting child translation " + node.getChildrenNodes().size());

            for (WebNode child : node.getChildrenNodes()) {
                Log.debug("translating "+child.getUrl());
                 if (!super.isRunning()&&Configuration.doIStopOnFatalTranslationError) return;

                deepTranslate(child);
                Log.info(getSynchronizer().getFutures().size());
            }
        }
    }

}
