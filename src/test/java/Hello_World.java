import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Hello_World {
    @Test
    public void helloWorldTest() {

        System.out.println("hello world");
        assertEquals(1+1,2);

    }

    @Test
    public void mainTest() {
        main.helloWorld("hello main");
        assertEquals( 1,1);
    }
}


