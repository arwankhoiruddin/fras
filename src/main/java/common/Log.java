package common;

public class Log {
    public static void display(String disp) {
        if (MRConfigs.displayLog)
            System.out.println(disp);
    }

    public static void display(Object disp) {
        if (MRConfigs.displayLog)
            System.out.println(disp);
    }

    public static void debug(String disp) {
        if (MRConfigs.debugLog)
            System.out.println(disp);
    }

    public static void debug(Object disp) {
        if (MRConfigs.debugLog)
            System.out.println(disp);
    }

}
