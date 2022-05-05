package common;

import org.codehaus.plexus.util.FileUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

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
        if (MRConfigs.debugLog) {
//            System.out.println(disp);
            disp = disp + "\n";
            try {
                Files.write(Paths.get("log.txt"), disp.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void debug(Object disp) {
        if (MRConfigs.debugLog)
            System.out.println(disp);
    }

}
