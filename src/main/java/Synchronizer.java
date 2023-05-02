import java.net.http.HttpResponse;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Synchronizer {
    private ConcurrentLinkedDeque<CompletableFuture<HttpResponse<String>>> futures = new ConcurrentLinkedDeque<>();
    private String message="";
    private Thread thread;
    private Callback callback;
    private static ConcurrentLinkedDeque<Synchronizer> blockingSynchronizers=new ConcurrentLinkedDeque<>();
  
    
    public static void joinAll(){
        System.out.println(blockingSynchronizers.size());
            for (Synchronizer s: blockingSynchronizers
                 ) {
                try {
                    System.out.println(s.message);
                    s.thread.join();

                } catch (InterruptedException e) {
                    s.callback.onError(e);
                }
            }
            blockingSynchronizers.clear();

    }

    public  Thread createBlockedTask(Task task, Callback callback){
        this.thread= new Thread(() -> {
            try {
                task.execute();
                waitForAllRequests();
            } catch (Exception e) {
                callback.onError(e);
            }
            callback.onComplete();
        });

     this.callback=callback;
     blockingSynchronizers.offer(this);
     return thread;
        
    }
    public void waitForAllRequests(){
        //as this is no operating systems course i handeled joining for threads quite liberally.
        //every second it is checked if no further requests are called
        int keepAlive = 0;
        int temp = 0;
        while (getFutures().size() > 0) {
            try {
                Thread.sleep(1000);

                if (temp == getFutures().size()) {
                    keepAlive++;
                } else {
                    keepAlive = 0;
                }

                temp = getFutures().size();

                //if for timeout seconds no request is added or removed all requests are cancelled
                if (keepAlive > Configuration.CLIENT_TIMEOUT_IN_SECONDS) {
                    System.out.println("remaining requests cancelled due to timeout");
                    killAllFutures();
                    break;
                }
                System.out.println(getFutures().size() + " requests active " +message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //technically not neccessary but for peace of mind
        killAllFutures();
    }
    public void killAllFutures() {
        //cancelling all requests that fell under unexpected error cases
        while (futures.size() > 0) {
            futures.pop().cancel(true);
        }
    }

    public void offerFuture(CompletableFuture<HttpResponse<String>> future){
        getFutures().offer(future);
    }

    public void removeFuture(CompletableFuture<HttpResponse<String>> f) {
        getFutures().remove(f);
    }

    public ConcurrentLinkedDeque<CompletableFuture<HttpResponse<String>>> getFutures() {
        return futures;
    }

    public void setFutures(ConcurrentLinkedDeque<CompletableFuture<HttpResponse<String>>> futures) {
        this.futures = futures;
    }

    public void setIntervalMessage(String s) {
        this.message=s;
    }
}
