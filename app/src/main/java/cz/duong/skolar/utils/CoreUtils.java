package cz.duong.skolar.utils;

/**
 * Created by David on 10. 4. 2014.
 */
public class CoreUtils {
    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
