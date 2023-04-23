
import org.junit.Before;

import org.junit.Test;
import org.mockito.*;

import java.io.*;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.*;


public class MarkdownFactoryTest {
    @Mock
    public WebNode webNodeMock;
    @InjectMocks
    public MarkdownFactory markdownFactory;

    public String url;
    public int depth;
    public String defaultHeader = "I am a leaf and hence have no header";
    public ConcurrentLinkedDeque<WebNode> childrenNodes;


    @Before
    public void init(){
        markdownFactory = new MarkdownFactory();
        openMocks(this);

        childrenNodes = new ConcurrentLinkedDeque<>();
        /*childrenNodes.offer(webNodeMock);
        childrenNodes.offer(webNodeMock);
*/
        when(webNodeMock.getUrl()).thenReturn("");
        when(webNodeMock.getDepth()).thenReturn(1);
        when(webNodeMock.isSuccessful()).thenReturn(false);
        when(webNodeMock.getHeader()).thenReturn(defaultHeader);
        when(webNodeMock.getChildrenNodes()).thenReturn(childrenNodes);

        depth=3;
        Configuration.setMaxCrawlDepth(depth);
        url = "https://www.bodofoto.at/";
        Configuration.setRootUrl(url);

    }

    @Test
    public void getFormatTest(){
        System.out.println(markdownFactory.getFormat(webNodeMock));
    }

    @Test
    public void getMarkdownStringTest() {
        markdownFactory.getMarkdownString(webNodeMock);
        verify(webNodeMock, times(1)).getUrl();
    }

    @Test
    public void getMarkdownStringTest2(){
        //System.out.println(markdownFactory.getMarkdownString(webNodeMock));
        StringBuilder expected = new StringBuilder("## ---> ** <br>\n" + "I am a leaf and hence have no header\n");
        assertEquals(expected, markdownFactory.getMarkdownString(webNodeMock));
    }

    @Test
    public void checkCreatedMarkdownFile(){
            String filePath = markdownFactory.path.toString();
            String markdownFileContent = "";
            try {
                FileReader fileReader = new FileReader(filePath);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                markdownFileContent += bufferedReader.readLine();
                bufferedReader.close();
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            assertEquals(" -> **"+url+"** <br>", markdownFileContent);
    }

}
