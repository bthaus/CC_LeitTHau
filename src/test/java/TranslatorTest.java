import org.checkerframework.checker.signature.qual.DotSeparatedIdentifiersOrPrimitiveType;
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
    public void setUo(){
        translator = new Translator("DE");
        openMocks(this);


        childrenNodes = new ConcurrentLinkedDeque<>();

        when(webNodeMock.getChildrenNodes()).thenReturn(childrenNodes);
    }
    @Test
    public void checkTranslationAPITest(){
        assertEquals (Configuration.doIHaveATranslationApiKey, translator.checkForTranslationApiKey());
    }

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
}
