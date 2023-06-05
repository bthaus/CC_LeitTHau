import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final DateFormat dform = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

    public static void err(Object o){
        printTime();
        System.err.println(o);
    }
    public static void stackTrace(Exception e){
        e.printStackTrace();
    }
    public static void info(Object o){
        printTime();
        System.out.println(o);
    }
    public static void debug(Object o){
        printTime();
        System.out.println(ANSI_BLUE+o+ANSI_RESET);
    }
    private static void printTime(){
        System.out.print(dform.format(new Date())+" | ");
    }
}
