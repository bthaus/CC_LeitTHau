import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class Translator {
    private String language;
    private String errorMessage ="";


    public boolean checkForTranslationApiKey(){
        return Data.doIHaveATranslationApiKey;

    }
    public void translateChildren(String language){
        this.language = language;
        if(checkForTranslationApiKey()) {
            for (WebsiteData child : Data.getChildren()) {
                translate(child);
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

    public void translate(WebsiteData child){
        JsonHelper.TranslationRequestBody body = JsonHelper.getTranslationRequestBody(child.getHeader(),"en", language);
        String bodyString = JsonHelper.getJsonString(body);

        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(Data.TRANSLATION_URI)).POST(HttpRequest.BodyPublishers.ofString(bodyString)).headers("content-type","application/json","X-RapidAPI-Key", Data.TRANSLATION_API_KEY,"X-RapidAPI-Host", Data.TRANSLATION_API_HOST).build();
        CompletableFuture<HttpResponse<String>> response = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(Data.CLIENT_TIMEOUT_IN_SECONDS)).build().sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
        Data.getFutures().offer(response);

        response.thenAcceptAsync((res) -> {
            child.removeFuture(response);
            String[] translation = res.body().split("translatedText\": \"");
            errorMessage = translation[0];
            child.setHeader(translation[1].substring(0, translation[1].lastIndexOf("\"")));
        }).exceptionally((exception) -> {
            if(!Data.getFutures().isEmpty()) {
                WebsiteData.killAllFutures();
                System.out.println(errorMessage);
            }
            return null;
        }).join();
    }
}
