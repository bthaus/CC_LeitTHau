import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MarkdownFactory {



    public void createMarkdownFile(WebsiteData root){

        Path path = Paths.get("src/main/resources/markdown.md");

        try {
            Files.writeString(path, getMarkdownString(root), StandardCharsets.UTF_8);
        }catch (IOException ex) {
            System.out.print("Invalid Path");
        }
        System.out.println(config.successes + " successes");
        System.out.println(config.failures + " failures");
    }

    public StringBuilder getMarkdownString(WebsiteData node){
        StringBuilder markdownString = new StringBuilder();
            markdownString.append(getFormat(node));
        for (WebsiteData child:node.getChildren()){
            markdownString.append(getMarkdownString(child));
            //TODO debug feature delete
            if (child.isSuccessful()) config.successes++;
            else config.failures++;
        }
        return markdownString;
    }

    public String getFormat(WebsiteData node){
        String formattedLine = "";
        formattedLine = concatNElements(formattedLine,"#", config.getCrawlDepth()-node.getDepth()-1).concat(" ");
        formattedLine = concatNElements(formattedLine,"-", config.getCrawlDepth()-node.getDepth());

        if(node.isSuccessful()){
            formattedLine = formattedLine.concat("> **" + node.getUrl() + "** <br>\n");
        }else{
            formattedLine = formattedLine.concat("> *" + node.getUrl() + "* <br>\n");
        }
        formattedLine=formattedLine.concat(node.getHeader());

        return formattedLine;
    }

    private String concatNElements(String word, String value, int repetitions){     //TODO find better methodname and parameter name
        for (int i = 0; i <= repetitions; i++) {
            word = word.concat(value);
        }
        return word;
    }
}
