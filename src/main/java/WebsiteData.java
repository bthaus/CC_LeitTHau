import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;

public class WebsiteData {
    static int successes=0;
    static int failures=0;
    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public WebsiteData(HttpHeaders header,String address, int depth,boolean success) {
        this.address=address;
        this.header = header.toString();
        this.depth = depth;
        this.successfull=success;
        if(success)successes++;
        else failures++;
    }
    public WebsiteData(HttpHeaders header,String address, int depth,boolean success,String errorMessage) {
        this.address=address;
        this.header = header.toString();
        this.depth = depth;
        this.successfull=success;
        this.errorMessage=errorMessage;
    }
    public void print(){
        System.out.println("adress: "+address+" depth: "+depth+" headers: "+header+" error message: "+errorMessage);
    }
    public String getMarkdownString(){
        String retval="";
        retval=addNStrings(retval,"#", main.CRAWL_DEPTH-depth-1).concat(" ");
        retval=addNStrings(retval,"-",main.CRAWL_DEPTH-depth);
        if(successfull){
            retval=retval.concat("> **"+address+"** <br>\n");
        }else{
            retval=retval.concat("> *"+address+"* <br>\n");
        }

        for (WebsiteData child:children
             ) {
            retval=retval.concat(child.getMarkdownString());
        }


        return retval;
    }
    private String addNStrings(String word,String value,int repetitions){

        for (int i = 0; i <= repetitions; i++) {
            word=word.concat(value);
        }
        return word;
    }

    public void translateAll(String language){
        this.translate(language);
        for (WebsiteData child:children
             ) {
            child.translateAll(language);
        }
    }

    /* formatted according to example response on deepl.com/docs-api:
      {
          "translations": [
          {
              "detected_source_language": "EN",
                  "text": "Hallo, Welt!"
          }
]
      }*/
    public void translate(String language){

        HttpRequest req = HttpRequest.newBuilder(URI.create(main.TRANSLATION_URI)).POST(HttpRequest.BodyPublishers.ofString(header +"&target_lang="+language)).header("Authorization",main.TRANSLATION_API_KEY).build();
        CompletableFuture<HttpResponse<String>> response = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(main.CLIENT_TIMEOUT_IN_SECONDS)).build().sendAsync(req, HttpResponse.BodyHandlers.ofString());
        main.futures.offer(response);
        response.thenAcceptAsync((res)-> {
            main.futures.remove(response);
            this.header=res.body().substring(res.body().indexOf("text\": \""),res.body().indexOf("\"\n}\n]\n}"));
        });

    }
    String address;
    String errorMessage;
    String header;
    int depth;
    boolean successfull;
    ConcurrentLinkedDeque<WebsiteData> children=new ConcurrentLinkedDeque<>();
}
