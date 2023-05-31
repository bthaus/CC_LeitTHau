import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;

public class SynchronizerTest {

    WebNode webNode;
    CompletableFuture<HttpResponse<String>> testFuture;

    @Before
    public void init(){
        webNode = new WebNode("Url", 2);
        testFuture = new CompletableFuture<>();
    }
    @After
    public void cleanUp(){
        webNode = null;
        testFuture = null;
    }

    @Test
    public void offerFutureTest(){
        int previousSize = webNode.getSynchronizer().getFutures().size();
        webNode.getSynchronizer().offerFuture(testFuture);
        assertEquals(previousSize+1, webNode.getSynchronizer().getFutures().size());
    }

    @Test
    public void removeFutureTest(){
        webNode.getSynchronizer().offerFuture(testFuture);
        int previousSize = webNode.getSynchronizer().getFutures().size();
        webNode.getSynchronizer().removeFuture(testFuture);
        assertEquals(previousSize-1, webNode.getSynchronizer().getFutures().size());
    }

    @Test
    public void killAllFuturesTest(){
        webNode.getSynchronizer().offerFuture(testFuture);
        webNode.getSynchronizer().offerFuture(testFuture);
        webNode.getSynchronizer().offerFuture(testFuture);
        webNode.getSynchronizer().killAllFutures();
        assertTrue(webNode.getSynchronizer().getFutures().isEmpty());
    }


}
