import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
public class Translator {
    private final String language;
    private static LinkedList<Translator> translators=new LinkedList<>();
    private Synchronizer synchronizer=new Synchronizer();
    private WebNode webNode;
    private Task task;
    private Thread thread;
    private Callback callback;

    private String possibleErrorMessage = "";
    private boolean running;

    private Translator(WebNode webNode,String language) {
        this.language = language;
        this.webNode=webNode;
        this.task= () -> deepTranslate(webNode);
        synchronizer.setIntervalMessage(webNode.getUrl()+"'s nodes are being translated");

    }

    static Translator createAndStartTranslator(WebNode webnode, String language, Callback callback){
        Translator translator=new Translator(webnode,language);
        translator.startNonBlocking(callback);
        translators.push(translator);
        return translator;
    }
    static void joinAll(){
        for (Translator tra:translators) {
            tra.join();
        }
    }

    public boolean checkForTranslationApiKey(){
        return Configuration.doIHaveATranslationApiKey;
    }


    public void deepTranslate(WebNode node){
        translate(node);
        if (checkForTranslationApiKey()) {
            for (WebNode child : node.getChildrenNodes()) {
                if (!running) return;

                deepTranslate(child);
                System.out.println(synchronizer.getFutures().size());
            }
        }
    }

    /** formatted according to example response on deepl.com/docs-api:
     {
     "translations": [
     {
     "detected_source_language": "EN",
     "text": "Hallo, Welt!"
     }
     ]
     }*/
    public void startNonBlocking(Callback callback){
        this.callback=callback;
        this.thread=synchronizer.createBlockedTask(task,callback);
        this.thread.start();

    }

    public void join(){
        try {
            this.thread.join();
        } catch (InterruptedException ignored) {

        }
    }

    public void translate(WebNode node) {
        JsonHelper.TranslationRequestBody body = JsonHelper.getTranslationRequestBody(node.getHeader(),"en", language);
        String bodyString = JsonHelper.getJsonString(body);

        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(Configuration.TRANSLATION_URI)).POST(HttpRequest.BodyPublishers.ofString(bodyString)).headers("content-type","application/json","X-RapidAPI-Key", Configuration.TRANSLATION_API_KEY,"X-RapidAPI-Host", Configuration.TRANSLATION_API_HOST).build();
        CompletableFuture<HttpResponse<String>> future = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(Configuration.CLIENT_TIMEOUT_IN_SECONDS)).build().sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
       synchronizer.offerFuture(future);

        future.thenAcceptAsync((res) -> {
          synchronizer.removeFuture(future);
           String[] translation = res.body().split("translatedText\": \"");
           possibleErrorMessage = translation[0];

            handlePossibleFatalError(res);

            node.setHeader(translation[1].substring(0, translation[1].lastIndexOf("\"")));

        }).exceptionally((exception) -> {
            if (!synchronizer.getFutures().isEmpty()) {
                System.out.println(possibleErrorMessage);
                stop();
                synchronizer.killAllFutures();
            }
            return null;
        });


    }

    private void stop(){
        this.running=false;
    }

    private void handlePossibleFatalError(HttpResponse<String> res)  {
        if (res.statusCode() > Configuration.LOWEST_FATAL_STATUS_CODE) {
            System.out.println(possibleErrorMessage);
            stop();
            synchronizer.killAllFutures();
            callback.onError(new Exception("Translating failed fatally with error code: " + res.statusCode() + " on Url: " + res.uri()));
        }
    }
}
