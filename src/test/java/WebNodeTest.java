import org.junit.After;
import org.junit.Test;

import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;

public class WebNodeTest {

    public WebNode webNode;

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

    //Crawl is testet with google, facebook and wikipedia. if all of these tests go wrong, we can assume,
    // it's the webscrawlers fault as it is very unlikely that all these 3 sites are down

/*
    @Test
    public void crawlFacebookTest(){
        webNode = new WebNode("https://www.facebook.com/", 2);
        int comparisonValue = webNode.getChildrenNodes().size();

        webNode.crawl();
      //  webNode.waitForRequests();

        assertNotEquals(comparisonValue, webNode.getChildrenNodes().size());

    }

    @Test
    public void crawlWikipediaTest(){
        webNode = new WebNode("https://de.wikipedia.org/wiki/Wikipedia:Hauptseite", 2);
        int comparisonValue = webNode.getChildrenNodes().size();

        webNode.crawl();
       // webNode.waitForRequests();

        assertNotEquals(comparisonValue, webNode.getChildrenNodes().size());
    }
*/
    @Test
    public void createRequestExceptionTest(){
        webNode = new WebNode("", 1);

        assertThrows(IllegalArgumentException.class, () -> {
            webNode.createRequest();
        });
    }

}
