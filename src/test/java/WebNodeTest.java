import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class WebNodeTest {

    public WebNode webNode;

    @Mock
    Synchronizer synchronizerMock;

    @Before
    public void setUp (){
        synchronizerMock = mock (Synchronizer.class);
    }

    @After
    public void cleanUp(){
        webNode = null;
    }

    @Test
    public void constructorTest(){
        webNode = new WebNode("url", 2);
        assertEquals(webNode.getTries(), 1);
    }

    @Test
    public void checkIsBaseCaseFirstRecursionTest(){
        webNode = new WebNode("url", 0);
        assertTrue(webNode.isBaseCase());
    }

    @Test
    public void checkIsBaseCaseAlreadyCrawledTest(){
        webNode = new WebNode("url", 1);
        WebNode.urlList.add("url");

        assertTrue(webNode.isBaseCase());
        WebNode.urlList.remove("url");
    }

    @Test
    public void checkIsBaseCaseMaxTriesTest(){
        webNode = new WebNode("url", 1);
        webNode.setTries(4);

        assertTrue(webNode.isBaseCase());
    }
    @Test
    public void checkIsBaseCaseMaxTriesTest2(){
        webNode = new WebNode("url", 1);
        webNode.setTries(4);

        assertTrue(webNode.isBaseCase() && !webNode.isSuccessful());
    }

    @Test
    public void getNameTest(){
        webNode = new WebNode("https://www.testURL.at", 0);
        assertEquals("www.testURL.at", webNode.getName());
    }

    @Test
    public void setHttpsHeaderIsNull(){
        webNode = new WebNode("url", 0);
        webNode.setHeader((HttpHeaders) null);
        assertEquals("no header", webNode.getHeader());
    }

    @Test
    public void createRequestTest(){            //TODO not an actual test... can i even test it?
        webNode = new WebNode("https://www.testURL.at", 1);
        CompletableFuture<HttpResponse<String>> actual = webNode.createRequest();
        System.out.println(actual);
    }

    @Test
    public void createRequestExceptionTest(){
        webNode = new WebNode("", 1);

        assertThrows(IllegalArgumentException.class, () -> {
            webNode.createRequest();
        });
    }

    @Test
    public void prepareForCrawlTest(){
        int before = WebNode.urlList.size();
        webNode = new WebNode("https://www.url.at", 1);
        webNode.prepareForCrawl();
        int after = WebNode.urlList.size();

        assertTrue(after > before);
    }

   /* @Test
    public void handleResponseTest(){
        webNode = new WebNode("https://www.bodofoto.at", 2);
        webNode.prepareForCrawl();
        webNode.setSynchronizer(synchronizerMock);
        webNode.handleResponse();

        verify(synchronizerMock, times((1))).removeFuture(webNode.createRequest());
    }*/

}
