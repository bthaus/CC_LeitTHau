import org.junit.After;
import org.junit.Before;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

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
/*
    @Test
    public void offerFutureTest(){
        int previousSize = webNode.getFutures().size();
        webNode.offerFuture(testFuture);
        assertEquals(previousSize+1, webNode.getFutures().size());
    }

    @Test
    public void removeFutureTest(){
        webNode.offerFuture(testFuture);
        int previousSize = webNode.getFutures().size();
        webNode.removeFuture(testFuture);
        assertEquals(previousSize-1, webNode.getFutures().size());
    }

    @Test
    public void killAllFuturesTest(){
        webNode.offerFuture(testFuture);
        webNode.offerFuture(testFuture);
        webNode.offerFuture(testFuture);
        webNode.killAllFutures();
        assertTrue(webNode.getFutures().isEmpty());
    }
*/}
