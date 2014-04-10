package cz.duong.skolar.utils;

import android.graphics.Color;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by David on 9. 4. 2014.
 */
public class SchoolUtils {
    public static int subjectToColor(String subject) {

        int hash = 0;

        final int length = subject.length();
        for (int offset = 0; offset < length; ) {
            final int codepoint = subject.codePointAt(offset);

            hash = codepoint + ((hash << 5) - hash);

            // do something with the codepoint

            offset += Character.charCount(codepoint);
        }

        String string = (Integer.toHexString((hash>>24)&0xFF)
                        + Integer.toHexString((hash>>16)&0xFF)
                        + Integer.toHexString((hash>>8)&0xFF)
                        + Integer.toHexString(hash&0xFF))
                        .substring(0, 6);

        return Color.parseColor("#" + string);
    }

    public static String shortenSubject(String subject) {
        Map<String, String> exceptions = new HashMap<String, String>();
        exceptions.put("čjl", "Čj");
        exceptions.put("evh", "Eh");
        exceptions.put("evv", "Ev");

        List<String> addIn = Arrays.asList(new String[]{"i", "y", "í", "ý"});

        String[] words = (Character.toUpperCase(subject.charAt(0)) + subject.substring(1)).split(" ");

        StringBuilder builder = new StringBuilder();

        if(words.length > 1) {
            for(String s : words) {
                if(s.length() > 1) {
                    builder.append(s.charAt(0));
                }
            }
        } else {
            String first = Character.toString(subject.charAt(0));
            String second = Character.toString(subject.charAt(1));

            builder.append(first);

            if(addIn.contains(second)) {
                builder.append(second);
            } else if (first.equals("C") && second.equals("h")) {
                builder.append("h");
            }
        }

        String result = builder.toString();

        if(exceptions.containsKey(result.toLowerCase())) {
            return exceptions.get(result.toLowerCase());
        }

        return result;
    }
}
