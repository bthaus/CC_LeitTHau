import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MarkdownFactory {

    public void createMarkdownFile(WebNode root){
        Path path = Paths.get("src/main/resources/markdown.md");

        try {
            Files.writeString(path, getMarkdownString(root), StandardCharsets.UTF_8);
        }catch (IOException ex) {
            System.out.print("Invalid Path");
        }

        System.out.println(Configuration.successes + " successes");
        System.out.println(Configuration.failures + " failures");
    }

    public StringBuilder getMarkdownString(WebNode node){
        StringBuilder markdownString = new StringBuilder();
            markdownString.append(getFormat(node));
        for (WebNode child : node.getChildrenNodes()){
            markdownString.append(getMarkdownString(child));

            //TODO debug feature delete
            if (child.isSuccessful()) Configuration.successes++;
            else Configuration.failures++;
        }
        return markdownString;
    }

    public String getFormat(WebNode node){
        String formattedLine = "";
        formattedLine = concatNElements(formattedLine,"#", Configuration.getMaxCrawlDepth()-node.getDepth()-1).concat(" ");
        formattedLine = concatNElements(formattedLine,"-", Configuration.getMaxCrawlDepth()-node.getDepth());

        if   (node.isSuccessful()){
            formattedLine = formattedLine.concat("> **" + node.getUrl() + "** <br>\n");
        }else{
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
