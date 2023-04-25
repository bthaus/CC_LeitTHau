import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JsonHelperTest {
    @Test
    public void testJsonString(){
        JsonHelper.TranslationRequestBody body= JsonHelper.getTranslationRequestBody("qtest","sourcetest","targettest");
        assertEquals("{\"q\":\"qtest\",\"source\":\"sourcetest\",\"target\":\"targettest\",\"format\":\"text\"}",JsonHelper.getJsonString(body));
    }

}
