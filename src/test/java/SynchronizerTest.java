import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.Mock;

public class SynchronizerTest {

    public WebNode webNode;
    public CompletableFuture<HttpResponse<String>> testFuture;

    @Mock
    public WebNode webNodeMock;
    public Synchronizer synchronizer;

    @Before
    public void init() {
        webNode = new WebNode("Url", 2);
        testFuture = new CompletableFuture<>();

        webNodeMock = mock(WebNode.class);
        synchronizer = new Synchronizer();

        when(webNodeMock.getSynchronizer()).thenReturn(synchronizer);

    }

    @After
    public void cleanUp() {
        webNode = null;
        testFuture = null;
    }

    @Test
    public void offerFutureTest() {
        int previousSize = webNode.getSynchronizer().getFutures().size();
        webNode.getSynchronizer().offerFuture(testFuture);
        assertEquals(previousSize + 1, webNode.getSynchronizer().getFutures().size());
    }

    @Test
    public void removeFutureTest() {
        webNode.getSynchronizer().offerFuture(testFuture);
        int previousSize = webNode.getSynchronizer().getFutures().size();
        webNode.getSynchronizer().removeFuture(testFuture);
        assertEquals(previousSize - 1, webNode.getSynchronizer().getFutures().size());
    }

    @Test
    public void killAllFuturesTest() {
        webNode.getSynchronizer().offerFuture(testFuture);
        webNode.getSynchronizer().offerFuture(testFuture);
        webNode.getSynchronizer().offerFuture(testFuture);
        webNode.getSynchronizer().killAllFutures();
        assertTrue(webNode.getSynchronizer().getFutures().isEmpty());
    }

    @Test
    public void onErrorCallbackTest() {
        final boolean[] thrown = {false};
        synchronizer.createNonBlockingTask(() -> {
            throw new RuntimeException("test");
        }, new Callback() {
            @Override
            public void onComplete(Object o) {

            }

            @Override
            public void onError(Exception e) {
                thrown[0] = true;
                System.out.println("error thrown in test");
                assertEquals("test", e.getMessage());

            }
        });
        synchronizer.startTask();
        synchronizer.join();
        assertTrue(thrown[0]);
    }

    @Test
    public void onCompleteCallbackTest() {
        final boolean[] called = {false};
        synchronizer.createNonBlockingTask(() -> System.out.println("Executing"), new Callback() {
            @Override
            public void onComplete(Object o) {
                System.out.println("task completed");
                called[0] = true;
            }

            @Override
            public void onError(Exception e) {
                fail();
            }
        });
        synchronizer.startTask();
        synchronizer.join();
        assertTrue(called[0]);

    }

    @Test
    public void startTaskTest() {
        final boolean[] executed = {false};
        synchronizer.createNonBlockingTask(() -> executed[0] = true, new Callback() {
            @Override
            public void onComplete(Object o) {
                assertTrue(executed[0]);
            }

            @Override
            public void onError(Exception e) {

            }
        });
        synchronizer.startTask();
        synchronizer.join();
    }

    @Test
    public void interruptTaskTest() {
        synchronizer.createNonBlockingTask(() -> {
            while (true) {
                Thread.sleep(100);
            }
        }, new Callback() {
            @Override
            public void onComplete(Object o) {
                System.out.println("on complete");
            }

            @Override
            public void onError(Exception e) {
                System.out.println("on error");
            }
        });
        synchronizer.startTask();
        synchronizer.getThread().interrupt();
        synchronizer.join();
    }

}
