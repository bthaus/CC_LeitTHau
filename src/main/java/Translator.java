import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class Translator {
    private String language;
    private String errorMessage ="";
    private boolean running;


    public Translator(String language) {
        this.language=language;
    }


    public boolean checkForTranslationApiKey(){
        return config.doIHaveATranslationApiKey;

    }
    public void deepTranslate(WebsiteData node){
        translate(node);
        if(checkForTranslationApiKey()) {
            for (WebsiteData child : node.getChildren()) {
                if(!running) return;
                deepTranslate(child);
                System.out.println(config.getFutures().size());
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
    public void translate(WebsiteData node){

        JsonHelper.TranslationRequestBody body = JsonHelper.getTranslationRequestBody(node.getHeader(),"en", language);
        String bodyString = JsonHelper.getJsonString(body);

        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(config.TRANSLATION_URI)).POST(HttpRequest.BodyPublishers.ofString(bodyString)).headers("content-type","application/json","X-RapidAPI-Key", config.TRANSLATION_API_KEY,"X-RapidAPI-Host", config.TRANSLATION_API_HOST).build();
        CompletableFuture<HttpResponse<String>> response = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(config.CLIENT_TIMEOUT_IN_SECONDS)).build().sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
        config.getFutures().offer(response);

        response.thenAcceptAsync((res) -> {
            node.removeFuture(response);
            if (isFatal(res)) return;

            String[] translation = res.body().split("translatedText\": \"");
            errorMessage = translation[0];
            node.setHeader(translation[1].substring(0, translation[1].lastIndexOf("\"")));

        }).exceptionally((exception) -> {

            if(!config.getFutures().isEmpty()) {
                stop();
                config.killAllFutures();
            }

            return null;
        }).join();

    }
    private boolean isFatal(HttpResponse<String> res){
        if (res.statusCode()>299){
            System.out.println(errorMessage);
            stop();
            config.killAllFutures();
            return true;
        }
        return false;
    }
}
