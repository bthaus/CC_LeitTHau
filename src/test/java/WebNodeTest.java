import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class WebNodeTest {

    public WebNode webNode;

    @After
    public void cleanUp(){
        webNode = null;
    }

    @Test
    public void constructorTest(){
        webNode = new WebNode("url", 2, true);
        assertEquals(webNode.getTries(), 1);
    }

    @Test
    public void checkIsBaseCaseFirstRecursionTest(){
        webNode = new WebNode("url", 0, true);
        assertTrue(webNode.isBaseCase());
    }

    @Test
    public void checkIsBaseCaseAlreadyCrawledTest(){
        webNode = new WebNode("url", 1, true);
        WebNode.urlList.add("url");

        assertTrue(webNode.isBaseCase());
        WebNode.urlList.remove("url");
    }

    //Crawl is testet with google, facebook and wikipedia. if all of these tests go wrong, we can assume,
    // it's the websCrawler's fault as it is very unlikely that all these 3 sites are down
    @Test
    public void crawlGoogleTest(){
        webNode = new WebNode("https://www.google.at", 2, true);
        int comparisonValue = webNode.getChildrenNodes().size();

        webNode.crawl();
        webNode.waitForRequests();

        assertNotEquals(comparisonValue, webNode.getChildrenNodes().size());
    }

    @Test
    public void crawlFacebookTest(){
        webNode = new WebNode("https://www.facebook.com/", 2, true);
        int comparisonValue = webNode.getChildrenNodes().size();

        webNode.crawl();
        webNode.waitForRequests();

        assertNotEquals(comparisonValue, webNode.getChildrenNodes().size());
    }

    @Test
    public void crawlWikipediaTest(){
        webNode = new WebNode("https://de.wikipedia.org/wiki/Wikipedia:Hauptseite", 2, true);
        int comparisonValue = webNode.getChildrenNodes().size();

        webNode.crawl();
        webNode.waitForRequests();

        assertNotEquals(comparisonValue, webNode.getChildrenNodes().size());
    }

    @Test
    public void createRequestExceptionTest(){
        webNode = new WebNode("", 1, true);

        assertThrows(IllegalArgumentException.class, () -> {
            webNode.createRequest();
        });
    }

}
