import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
    private static final String BLUE_FONT = "\u001B[34m";
    private static final String FONT_RESET = "\u001B[0m";
    private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public static void err(Object o) {
        printTime();
        System.err.println(o);
    }

    public static void stackTrace(Exception e) {
        e.printStackTrace();
    }

    public static void info(Object o) {
        printTime();
        System.out.println(o);
    }

    public static void debug(Object o) {
        printTime();
        System.out.println(BLUE_FONT + o + FONT_RESET);
    }

    private static void printTime() {
        System.out.print(dateFormat.format(new Date()) + " | ");
    }
}
