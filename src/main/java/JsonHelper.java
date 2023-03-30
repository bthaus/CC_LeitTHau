import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonHelper
{

    public static TranslationRequestBody getTranslationRequestBody(String q, String source, String target){
        return new TranslationRequestBody(q, source, target);
    }

    public static String getJsonString(Object o){
        ObjectMapper mapper=new ObjectMapper();
        String test="";
        try {
            test=mapper.writeValueAsString(
                    o
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return test;
    }

static class TranslationRequestBody {

    public TranslationRequestBody(String q, String source, String target) {
        this.q = q;
        this.source = source;
        this.target = target;
    }


    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getFormat() {
        return format;
    }


    String q;
    String source;
    String target;
    final String format="text";
}


}
