
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

        when(webNodeMock.getUrl()).thenReturn("");
        when(webNodeMock.getDepth()).thenReturn(2);
        when(webNodeMock.getHeader()).thenReturn(defaultHeader);
        when(webNodeMock.getChildrenNodes()).thenReturn(childrenNodes);

        depth=3;
        Configuration.setMaxCrawlDepth(depth);
        url = "https://www.bodofoto.at/";
        Configuration.setRootUrl(url);

    }

    @Test
    public void getFormatTest(){
        String actual = markdownFactory.getFormat(webNodeMock);
        assertTrue(actual.contains("> **"));
    }

    @Test
    public void getFormatVerifyTest(){
        when(webNodeMock.isSuccessful()).thenReturn(true);
        markdownFactory.getFormat(webNodeMock);
        verify(webNodeMock, times(1)).getUrl();
    }
    @Test
    public void getFormatUnsuccessfulTest(){
        when(webNodeMock.isSuccessful()).thenReturn(false);
        String actual = markdownFactory.getFormat(webNodeMock);
        assertTrue(actual.contains("> *"));
    }
    @Test
    public void getFormatUnsuccessfulVerifyTest(){
        when(webNodeMock.isSuccessful()).thenReturn(false);
        markdownFactory.getFormat(webNodeMock);
        verify(webNodeMock, times(1)).getUrl();
    }
    @Test
    public void getMarkdownStringTest(){
        when(webNodeMock.isSuccessful()).thenReturn(true);
        String actual = markdownFactory.getMarkdownString(webNodeMock).toString();
        assertTrue(actual.contains(webNodeMock.getHeader()) && actual.contains("> **"));
    }
    @Test
    public void getMarkdownStringVerifyTest() {
        markdownFactory.getMarkdownString(webNodeMock);
        verify(webNodeMock, times(1)).getUrl();
    }


    @Test
    public void checkCreatedMarkdownFile(){
        markdownFactory.createMarkdownFile(webNodeMock);
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

            assertEquals(" # " + "" + " <br>" , markdownFileContent);
    }

    @Test
    public void createMarkdownFileNoPathTest(){
        assertThrows(NullPointerException.class, () -> {
            markdownFactory.path.toString();
        });
    }

    @Test
    public void createMarkdownFileErrorMessage(){
        markdownFactory.createMarkdownFile(webNodeMock, "errorMessage");
        String filePath = markdownFactory.path.toString();
        String markdownFileContentWithError = "";
        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            markdownFileContentWithError += bufferedReader.readLine();
            bufferedReader.close();
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(" # An error happened when crawling this page.", markdownFileContentWithError);
    }

    /*
    @Test
    public void getMarkdownStringKidNodesTest(){
        childrenNodes.add(webNodeMock);
        childrenNodes.add(webNodeMock);

        markdownFactory.getMarkdownString(webNodeMock);
        verify(webNodeMock, times(1)).getChildrenNodes();

    }*/ //todo fix or delete
}
