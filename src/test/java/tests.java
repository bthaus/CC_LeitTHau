
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;



public class tests {
    @Test
    public void testJsonString(){
       JsonHelper.TranslationRequestBody body= JsonHelper.getTranslationRequestBody("qtest","sourcetest","targettest");
       assertEquals("{\"q\":\"qtest\",\"source\":\"sourcetest\",\"target\":\"targettest\",\"format\":\"text\"}",JsonHelper.getJsonString(body));
    }

    @Test
    public void testTranslation() {
        WebNode data=new WebNode("test header with testvalue");
        //TODO change data.translate("de");
        assertEquals("Testheader mit Testwert",data.getHeader());
    }


}


