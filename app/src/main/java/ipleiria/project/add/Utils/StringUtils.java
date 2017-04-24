package ipleiria.project.add.Utils;

import java.text.Normalizer;

/**
 * Created by BrunoJoséFonseca on 24/04/2017.
 */

public class StringUtils {
    public static String removeDiacriticalMarks(String string) {
        return Normalizer.normalize(string, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
