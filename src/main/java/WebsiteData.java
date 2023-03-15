import java.net.http.HttpHeaders;

public class WebsiteData {

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
    String address;
    String errorMessage;
    HttpHeaders header;
    int depth;
    boolean successfull;
}
