import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonHelper{

    public static TranslationRequestBody getTranslationRequestBody(String header, String sourceLanguage, String targetLanguage){
        return new TranslationRequestBody(header, sourceLanguage, targetLanguage);
    }

    public static String getJsonString(Object o){
        ObjectMapper mapper=new ObjectMapper();
        String jsonStr="";
        try {
            jsonStr=mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonStr;
    }

    static class TranslationRequestBody {
        private String header;
        private String sourceLanguage;
        private String targetLanguage;
        private final String format="text";

        public TranslationRequestBody(String header, String sourceLanguage, String targetLanguage) {
            this.header = header;
            this.sourceLanguage = sourceLanguage;
            this.targetLanguage = targetLanguage;
        }


        public String getHeader() {
            return header;
        }

        public void setHeader(String header) {
            this.header = header;
        }

        public String getSourceLanguage() {
            return sourceLanguage;
        }

        public void setSourceLanguage(String sourceLanguage) {
            this.sourceLanguage = sourceLanguage;
        }

        public String getTargetLanguage() {
            return targetLanguage;
        }

        public void setTargetLanguage(String targetLanguage) {
            this.targetLanguage = targetLanguage;
        }

        public String getFormat() {
            return format;
        }

    }

}
