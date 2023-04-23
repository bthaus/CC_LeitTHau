import org.checkerframework.checker.units.qual.A;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
        assertTrue(webNode.checkBaseCases());
    }
    @Test
    public void checkIsBaseCaseTriesTest(){
        webNode = new WebNode("url", 1, true);
        webNode.setTries(Configuration.getMaxCrawlDepth()+1);

        //TODO assertTrue(webNode.checkBaseCases());
    }
    @Test
    public void checkIsBaseCaseAlreadyCrawledTest(){
        webNode = new WebNode("url", 1, true);
        WebNode.urlList.add("url");

        assertTrue(webNode.checkBaseCases());
        WebNode.urlList.remove("url");
    }

    //Crawl is testet with google, facebook and wikipedia. if all of these tests go wrong, we can assume,
    // it's the webscrawlers fault as it is very unlikely that all these 3 sites are down
    @Test
    public void crawlGoogleTest(){
        webNode = new WebNode("https://www.google.at", 2, true);
        int comparisonValue = webNode.getChildrenNodes().size();

        webNode.crawl();
        webNode.waitForRequests();


        assertNotEquals(comparisonValue, webNode.getChildrenNodes().size());


       //test
        //1: if the number of Children increases
        //2: if Depth increases
        //3: if headers are put into it

        //ALSO NOTE THE THOUGHT PROCESS SEE BODO CHAT

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
