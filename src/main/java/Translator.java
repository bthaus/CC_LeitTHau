import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class Translator {
    private final String language;
    private final Synchronizer synchronizer;
    private String errorMessage ="";
    private boolean running;


    public Translator(String language, Synchronizer synchronizer) {
        this.language = language;
        this.synchronizer = synchronizer;
    }


    public boolean checkForTranslationApiKey(){
        return Configuration.doIHaveATranslationApiKey;
    }

    public void deepTranslate(Webnode node){
        translate(node);
        if(checkForTranslationApiKey()) {
            for (Webnode child : node.getChildrenNodes()) {
                if(!running) return;
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
    private void stop(){
        this.running=false;
    }
    public void translate(Webnode node){

        JsonHelper.TranslationRequestBody body = JsonHelper.getTranslationRequestBody(node.getHeader(),"en", language);
        String bodyString = JsonHelper.getJsonString(body);

        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(Configuration.TRANSLATION_URI)).POST(HttpRequest.BodyPublishers.ofString(bodyString)).headers("content-type","application/json","X-RapidAPI-Key", Configuration.TRANSLATION_API_KEY,"X-RapidAPI-Host", Configuration.TRANSLATION_API_HOST).build();
        CompletableFuture<HttpResponse<String>> response = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(Configuration.CLIENT_TIMEOUT_IN_SECONDS)).build().sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
        synchronizer.getFutures().offer(response);

        response.thenAcceptAsync((res) -> {
            synchronizer.removeFuture(response);
            if (isFatal(res)) return;

            String[] translation = res.body().split("translatedText\": \"");
            errorMessage = translation[0];
            node.setHeader(translation[1].substring(0, translation[1].lastIndexOf("\"")));

        }).exceptionally((exception) -> {
            if(!synchronizer.getFutures().isEmpty()) {
                stop();
                synchronizer.killAllFutures();
                System.out.println(errorMessage);
            }
            return null;
        }).join();

    }
    private boolean isFatal(HttpResponse<String> res){
        if (res.statusCode()>299){
            System.out.println(errorMessage);
            stop();
            synchronizer.killAllFutures();
            return true;
        }
        return false;
    }
}
