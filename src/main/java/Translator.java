import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
public class Translator {
    private final String language;
   private Synchronizer synchronizer = new Synchronizer();

    private String possibleErrorMessage = "";
    private boolean running;

    public Translator( String language) {
        this.language = language;
    }



    public boolean checkForTranslationApiKey() {
        return Configuration.doIHaveATranslationApiKey;
    }




//not part of assignment, but fits nonblockingstructure. if one wants to translate an array of strings that could be done the same way
    // as in webnodeTranslator. as its not part of the assignment this is just left as a stub
    public void startNonBlocking(Callback callback, String text) {
        synchronizer.createNonBlockingTask(() -> translateNonBlocking(text, callback), callback);
        synchronizer.startTask();
    }


    /**
     * formatted according to example response on deepl.com/docs-api:
     * {
     * "translations": [
     * {
     * "detected_source_language": "EN",
     * "text": "Hallo, Welt!"
     * }
     * ]
     * }
     */
    public void translateNonBlocking(String text, Callback callback ) {
        JsonHelper.TranslationRequestBody body = JsonHelper.getTranslationRequestBody(text, "en", language);
        String bodyString = JsonHelper.getJsonString(body);

        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(Configuration.TRANSLATION_URI)).POST(HttpRequest.BodyPublishers.ofString(bodyString)).headers("content-type", "application/json", "X-RapidAPI-Key", Configuration.TRANSLATION_API_KEY, "X-RapidAPI-Host", Configuration.TRANSLATION_API_HOST).build();
        CompletableFuture<HttpResponse<String>> future = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(Configuration.CLIENT_TIMEOUT_IN_SECONDS)).build().sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
        synchronizer.offerFuture(future);

        future.thenAcceptAsync((res) -> {
            synchronizer.removeFuture(future);
            String[] translation = res.body().split("translatedText\": \"");
            possibleErrorMessage = translation[0];
            handlePossibleFatalError(res);
            callback.onComplete(translation[1].substring(0, translation[1].lastIndexOf("\"")));

        }).exceptionally((exception) -> {
            if (!synchronizer.getFutures().isEmpty()) {
                synchronizer.removeFuture(future);
                callback.onError(new Exception(exception));
                stop();
                Log.err(possibleErrorMessage);
                synchronizer.killAllFutures();

            }
            return null;
        });


    }

    private void stop() {
        this.running = false;
    }

    private void handlePossibleFatalError(HttpResponse<String> res) {
        if (res.statusCode() > Configuration.LOWEST_FATAL_STATUS_CODE) {
            stop();
            Log.err(possibleErrorMessage);
            synchronizer.killAllFutures();
            synchronizer.onError(new Exception("Translating failed fatally with error code: " + res.statusCode() + " on Url: " + res.uri()));
        }
    }
}
