import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class Translator implements Syncer{
    private final String language;

    private String possibleErrorMessage = "";
    private boolean running;


    public Translator(String language) {
        this.language = language;

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
                System.out.println(getFutures().size());
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

    public void translate(WebNode node){
        JsonHelper.TranslationRequestBody body = JsonHelper.getTranslationRequestBody(node.getHeader(),"en", language);
        String bodyString = JsonHelper.getJsonString(body);

        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(Configuration.TRANSLATION_URI)).POST(HttpRequest.BodyPublishers.ofString(bodyString)).headers("content-type","application/json","X-RapidAPI-Key", Configuration.TRANSLATION_API_KEY,"X-RapidAPI-Host", Configuration.TRANSLATION_API_HOST).build();
        CompletableFuture<HttpResponse<String>> response = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(Configuration.CLIENT_TIMEOUT_IN_SECONDS)).build().sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
        offerFuture(response);

        response.thenAcceptAsync((res) -> {
           removeFuture(response);
           String[] translation = res.body().split("translatedText\": \"");
           possibleErrorMessage = translation[0];

           if (isFatal(res)) return;

           node.setHeader(translation[1].substring(0, translation[1].lastIndexOf("\"")));

        }).exceptionally((exception) -> {
            if (!getFutures().isEmpty()) {
                stop();
                killAllFutures();
            }
            return null;
        }).join();

    }

    private void stop(){
        this.running=false;
    }

    private boolean isFatal(HttpResponse<String> res){
        if (res.statusCode() > Configuration.LOWEST_FATAL_STATUS_CODE){
            System.out.println(possibleErrorMessage);
            stop();
            killAllFutures();
            return true;
        }
        return false;
    }
}
