import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonHelper{

    public static TranslationRequestBody getTranslationRequestBody(String header, String sourceLanguage, String targetLanguage){
        return new TranslationRequestBody(header, sourceLanguage, targetLanguage);
    }

    public static String getJsonString(Object o){
        ObjectMapper mapper = new ObjectMapper();
        String jsonStr = "";
        try {
            jsonStr = mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonStr;
    }

    static class TranslationRequestBody {
        private String q;
        private String source;
        private String target;
        private final String format = "text";

        public TranslationRequestBody(String q, String sourceLanguage, String target) {
            this.q = q;
            this.source = sourceLanguage;
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

    }

}
