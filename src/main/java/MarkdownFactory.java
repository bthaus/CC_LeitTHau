import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MarkdownFactory {
    public Path path;

    private void createMarkdownFileInternal(WebNode root, String headerMessage, String title){
        path = Paths.get("src/main/resources/"+root.getName()+title+".md");
        try {
            Files.writeString(path, " # "+headerMessage+" <br>\n "+ getMarkdownString(root), StandardCharsets.UTF_8);
        }catch (IOException ex) {
            System.out.println("Invalid Path");
        }
    }
    public void createMarkdownFile(WebNode root, String errorMessage){
        createMarkdownFileInternal(root,"An error happened when crawling this page.\n <br> " +
                "Errormessage: "+errorMessage+" <br> ",
                "WithError");
    }
    public void createMarkdownFile(WebNode root){
        createMarkdownFileInternal(root,"","");
    }

    public StringBuilder getMarkdownString(WebNode node){
        StringBuilder markdownString = new StringBuilder();
        markdownString.append(getFormat(node));

        for (WebNode child : node.getChildrenNodes()){
            markdownString.append(getMarkdownString(child));

            //for debugging and checks
            if (child.isSuccessful()) Configuration.successes++;
            else Configuration.failures++;
        }
        return markdownString;
    }

    public String getFormat(WebNode node){
        String formattedLine = "";
        formattedLine = concatNElements(formattedLine,"#", Configuration.getMaxCrawlDepth()-node.getDepth()-1).concat(" ");
        formattedLine = concatNElements(formattedLine,"-", Configuration.getMaxCrawlDepth()-node.getDepth());

        if (node.isSuccessful()){
            formattedLine = formattedLine.concat("> **" + node.getUrl() + "** <br>\n");
        } else{
            formattedLine = formattedLine.concat("> *" + node.getUrl() + "* <br>\n");
        }
        formattedLine = formattedLine.concat(node.getHeader() + "\n");

        return formattedLine;
    }

    private String concatNElements(String word, String value, int repetitions){
        for (int i = 0; i <= repetitions; i++) {
            word = word.concat(value);
        }
        return word;
    }
}
