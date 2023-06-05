import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.http.HttpResponse;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.code.tempusfugit.*;
import org.mockito.Mock;

public class SynchronizerTest {

    public WebNode webNode;
    public CompletableFuture<HttpResponse<String>> testFuture;

    @Mock
    public WebNode webNodeMock;
    public Synchronizer synchronizer;

    @Before
    public void init(){
        webNode = new WebNode("Url", 2);
        testFuture = new CompletableFuture<>();

        webNodeMock = mock (WebNode.class);
        synchronizer = new Synchronizer();

        when(webNodeMock.getSynchronizer()).thenReturn (synchronizer);

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
