package ipleiria.project.add.Utils;

import java.text.Normalizer;

/**
 * Created by BrunoJoséFonseca on 24/04/2017.
 */

public class StringUtils {

    /**
     * Removes/substitutes special characters, ie. atenção -> atencao
     */
    public static String replaceDiacriticalMarks(String string) {
        return Normalizer.normalize(string, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
