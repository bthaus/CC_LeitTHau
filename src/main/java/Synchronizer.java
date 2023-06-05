import lombok.Getter;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedDeque;
@Getter
public class Synchronizer {
    private ConcurrentLinkedDeque<CompletableFuture<HttpResponse<String>>> futures = new ConcurrentLinkedDeque<>();
    private String message="";
    private Thread thread;
    private Callback callback;
    private static final ConcurrentLinkedDeque<Synchronizer> blockingSynchronizers=new ConcurrentLinkedDeque<>();
  
    
    public static void joinAll(){
       Log.debug(blockingSynchronizers.size());
            for (Synchronizer s: blockingSynchronizers
                 ) {
                try {
                    Log.info(s.message);
                    s.thread.join();

                } catch (CompletionException e){
                    Log.err("fatal translation exception has been caught");
                }
                catch (InterruptedException e) {
                    s.callback.onError(e);
                }
            }
            blockingSynchronizers.clear();
    }

    public void createNonBlockingTask(Task task, Callback callback){
        this.callback=callback;
        blockingSynchronizers.offer(this);

        this.thread= new Thread(() -> {
            boolean thrown=false;
            try {
                task.execute();
                waitForAllRequests();
            } catch (Exception e) {
                thrown=true;
                Log.err("caught error in blocked task ");
                callback.onError(e);
            }
            callback.onComplete(thrown);
        });

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
                    Log.info("remaining requests cancelled due to timeout");
                    killAllFutures();
                    break;
                }
                Log.info(getFutures().size() + " requests active " +message);
            } catch (InterruptedException e) {
               Log.stackTrace(e);
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


    public void startTask() {
        this.thread.start();
    }

    public void onError(Exception exception) {
        this.callback.onError(exception);
    }

    public void join()  {
        try {
            this.thread.join();
        } catch (InterruptedException ignored) {

        }
    }
}
