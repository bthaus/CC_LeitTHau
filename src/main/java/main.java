import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class main {
   static BufferedReader in;
    static PrintWriter out;
    public static void helloWorld(String msg){
        System.out.println("hellö");

    }

    public static void main(String[] args) {
        try {


            HttpRequest.Builder builder= HttpRequest.newBuilder(URI.create("https://www.google.at/"));
            HttpRequest req=builder.GET().build();
            HttpClient client=HttpClient.newHttpClient();
            HttpResponse<String>res= client.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println(res);
            System.out.println("headers: "+res.headers().toString());

            System.out.println("body:"+res.body());


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
