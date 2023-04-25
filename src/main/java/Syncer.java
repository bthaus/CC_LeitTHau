import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;

public interface Syncer {
    //wrapper interface. technically i could move all methods from synchronizer in here and make them default,
    // but i want the option to still use the synchronizer class, even if this might cause some extra maintenance work
    Synchronizer synchronizer = new Synchronizer();
    default void waitForRequests(){
        synchronizer.waitForAllRequests();
    }
    default void offerFuture(CompletableFuture<HttpResponse<String>> future){
        synchronizer.offerFuture(future);
    }
    default void removeFuture(CompletableFuture<HttpResponse<String>> f) {
        synchronizer.removeFuture(f);
    }

    default void killAllFutures(){
        synchronizer.killAllFutures();
    }
    default ConcurrentLinkedDeque<CompletableFuture<HttpResponse<String>>> getFutures() {
        return synchronizer.getFutures();
    }
    default void setFutures(ConcurrentLinkedDeque<CompletableFuture<HttpResponse<String>>> futures) {
        synchronizer.setFutures(futures);
    }
}
