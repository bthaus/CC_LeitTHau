import com.google.api.services.translate.Translate;
import com.google.cloud.translate.Language;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class main {
    static ConcurrentLinkedDeque<WebsiteData> websiteData = new ConcurrentLinkedDeque<>();
    static ConcurrentLinkedDeque<String> urlList = new ConcurrentLinkedDeque<>();
    static ConcurrentLinkedDeque<String> errorUrls = new ConcurrentLinkedDeque<>();
    static ConcurrentLinkedDeque<CompletableFuture<HttpResponse<String>>> futures = new ConcurrentLinkedDeque<>();
    static ConcurrentLinkedDeque<CompletableFuture<HttpResponse<String>>> toremove = new ConcurrentLinkedDeque<>();
    static Scanner in = new Scanner(System.in);
    static final int CRAWL_DEPTH = 3;
    static final int CLIENT_TIMEOUT_IN_SECONDS = 3;

    public static void helloWorld(String msg) {

    }

    public static void main(String[] args) {

        for (String a : args
        ) {
            System.out.println(a);
        }
        crawl("https://www.gmx.at/", CRAWL_DEPTH);
        int keepAlive = 0;
        int temp = 0;
        while (futures.size() > 0) {
            try {
                Thread.sleep(1000);
                if (temp == futures.size()) {
                    keepAlive++;
                } else {
                    temp = 0;
                }
                temp = futures.size();
                if (keepAlive > CLIENT_TIMEOUT_IN_SECONDS) {
                    System.out.println("remaining requests cancelled due to timeout");
                    killAllFutures();
                }
                System.out.println(futures.size() + " crawlers running" + urlList.size() + " websites pinged and " + errorUrls.size() + " websites unsuccessfully pinged");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        killAllFutures();

        errorUrls.removeAll(urlList);
        for (WebsiteData data : websiteData
        ) {
            data.print();
        }
        System.out.println(websiteData.size() + " websites crawled");
        System.out.println(websiteData.size() - errorUrls.size() + " successfully visited sites");


    }

    public static void crawl(String url, int depth) {

        if (depth == 0) {
            return;
        }

        if (urlList.contains(url)) {
            return;
        }
        urlList.offer(url);
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url));
        HttpRequest req = builder.GET().build();
        CompletableFuture<HttpResponse<String>> response = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(CLIENT_TIMEOUT_IN_SECONDS)).build().sendAsync(req, HttpResponse.BodyHandlers.ofString());

        futures.offer(response);
        response.thenAcceptAsync((res) -> {

            websiteData.add(new WebsiteData(res.headers(), url, depth, true));
            removeFuture(response);
            String hrefs[] = res.body().split("href=\"");

            for (String a : hrefs
            ) {

                String link = null;
                try {
                    link = a.substring(0, a.indexOf("\""));
                } catch (Exception e) {
                    link = a;
                }
                if (link.contains("https://") || link.contains("http://")) {
                    crawl(link, depth - 1);


                }

            }

        }).exceptionally((exc) -> {
            // System.out.println("error happened with message "+exc.getMessage());
            removeFuture(response);
            if (!exc.getMessage().contains("CancellationException"))
                websiteData.offer(new WebsiteData(null, url, depth, false, exc.getMessage()));
            errorUrls.offer(url);
            return null;
        });


    }

    static void killAllFutures() {
        for (CompletableFuture<HttpResponse<String>> future : futures
        ) {
            future.cancel(true);
        }
    }

    static void removeFuture(CompletableFuture<HttpResponse<String>> f) {
        futures.remove(f);
    }
}

