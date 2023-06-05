import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.concurrent.ConcurrentLinkedDeque;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class TranslatorTest {

    @Mock
    public WebNode webNodeMock;

    @InjectMocks
    public Translator translator;

    public ConcurrentLinkedDeque<WebNode> childrenNodes;

    @Before
    public void setUp(){
        openMocks(this);
        childrenNodes = new ConcurrentLinkedDeque<>();
        when(webNodeMock.getChildrenNodes()).thenReturn(childrenNodes);

        Translator.createAndStartNonBlocking(webNodeMock, "DE", new Callback() {
            @Override
            public void onComplete() {
                System.out.println("all threads has been translated i think");  //TODO delete or change
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }
    @Test
    public void checkTranslationAPITest(){
        assertEquals (Configuration.doIHaveATranslationApiKey, translator.checkForTranslationApiKey());
    }
/*
    @Test
    public void deepTranslateWithChildTest(){
        childrenNodes.offer(webNodeMock);
        translator.deepTranslate(webNodeMock);
        verify(webNodeMock, times(1)).getChildrenNodes();

    }

    @Test
    public void deepTranslateWithChildrenTest(){
        childrenNodes.offer(webNodeMock);
        childrenNodes.offer(webNodeMock);
        childrenNodes.offer(webNodeMock);
        childrenNodes.offer(webNodeMock);
        translator.deepTranslate(webNodeMock);
        verify(webNodeMock, times(1)).getChildrenNodes();

    }

    @Test
    public void translateTest(){
        translator.translate(webNodeMock);
        verify(webNodeMock, times(1)).getHeader();
    }

    @Test
    public void translateHeaderTest() {
        WebNode data=new WebNode("test header with testvalue");
        Translator headerTranslator = new Translator("de");
        headerTranslator.translate(data);
        assertEquals("Testheader mit Testwert", data.getHeader());
    }
    */

}
