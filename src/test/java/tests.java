
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class tests {
    @Test
    public void helloWorldTest() {

        System.out.println("hello world");
        assertEquals(1+1,2);

    }
    @Test
    public void testJsonString(){
       JsonHelper.TranslationRequestBody body= JsonHelper.getTranslationRequestBody("qtest","sourcetest","targettest");
       assertEquals("{\"q\":\"qtest\",\"source\":\"sourcetest\",\"target\":\"targettest\",\"format\":\"text\"}",JsonHelper.getJsonString(body));
    }

    @Test
    public void testTranslation() {
        Webnode data=new Webnode("test header with testvalue");
        //TODO change data.translate("de");
        assertEquals("Testheader mit Testwert",data.getHeader());
    }


}


