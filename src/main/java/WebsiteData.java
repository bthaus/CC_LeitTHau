import java.net.http.HttpHeaders;
import java.util.concurrent.ConcurrentLinkedDeque;

public class WebsiteData {
    static int successes=0;
    static int failures=0;
    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public WebsiteData(HttpHeaders header,String address, int depth,boolean success) {
        this.address=address;
        this.header = header;
        this.depth = depth;
        this.successfull=success;
        if(success)successes++;
        else failures++;
    }
    public WebsiteData(HttpHeaders header,String address, int depth,boolean success,String errorMessage) {
        this.address=address;
        this.header = header;
        this.depth = depth;
        this.successfull=success;
        this.errorMessage=errorMessage;
    }
    public void print(){
        System.out.println("adress: "+address+" depth: "+depth+" headers: "+header+" error message: "+errorMessage);
    }
    public String getMarkdownString(){
        String retval="";
        retval=addNStrings(retval,"#", main.CRAWL_DEPTH-depth-1).concat(" ");
        retval=addNStrings(retval,"-",main.CRAWL_DEPTH-depth);
        if(successfull){
            retval=retval.concat("> **"+address+"** <br>\n");
        }else{
            retval=retval.concat("> *"+address+"* <br>\n");
        }

        for (WebsiteData child:children
             ) {
            retval=retval.concat(child.getMarkdownString());
        }


        return retval;
    }
    private String addNStrings(String word,String value,int repetitions){

        for (int i = 0; i <= repetitions; i++) {
            word=word.concat(value);
        }
        return word;
    }

    String address;
    String errorMessage;
    HttpHeaders header;
    int depth;
    boolean successfull;
    ConcurrentLinkedDeque<WebsiteData> children=new ConcurrentLinkedDeque<>();
}
