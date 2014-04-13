package cz.duong.skolar.utils;

import android.content.res.Resources;

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

    public static int convertDPtoPX(Resources resources, int dp) {
        float scale = resources.getDisplayMetrics().density;
        return (int) (scale * dp + 0.5f);
    }
}
