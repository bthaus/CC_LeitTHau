import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MarkdownFile {

    private ConcurrentLinkedDeque<WebsiteData> children;
    public void createMarkdownFile(ConcurrentLinkedDeque<WebsiteData> children){
        this.children = children;

        Path path = Paths.get("C:\\Users\\claud\\OneDrive\\Anlagen\\Uni\\Master\\1. Semester\\Clean Code\\assignment_1\\CC_LeitTHau\\src\\main\\markdown.md");

        try {
            Files.writeString(path, getMarkdownString(), StandardCharsets.UTF_8);
        }catch (IOException ex) {
            System.out.print("Invalid Path");
        }
        System.out.println(Data.successes + " successes");
        System.out.println(Data.failures + " failures");
    }

    public StringBuilder getMarkdownString(){
        StringBuilder markdownString = new StringBuilder();
        for (WebsiteData child:children){
            System.out.println(child);
            markdownString.append(getFormat(child));

            //TODO debug feature delete
            if (child.isSuccessful()) Data.successes++;
            else Data.failures++;

        }
        return markdownString;
    }

    public String getFormat(WebsiteData child){
        String formattedLine = "";
        formattedLine = addNStrings(formattedLine,"#", Data.getCrawlDepth()-child.getDepth()-1).concat(" ");
        formattedLine = addNStrings(formattedLine,"-",Data.getCrawlDepth()-child.getDepth());

        if(child.isSuccessful()){
            formattedLine = formattedLine.concat("> **" + child.getUrl() + "** <br>\n");
        }else{
            formattedLine = formattedLine.concat("> *" + child.getUrl() + "* <br>\n");
        }
        return formattedLine;
    }

    private String addNStrings(String word, String value, int repetitions){     //TODO find better methodname and parameter name
        for (int i = 0; i <= repetitions; i++) {
            word = word.concat(value);
        }
        return word;
    }
}
