package pt.arquivo.utils;

import java.util.regex.Pattern;

public class Utils {

    public static boolean urlValidator(String url) {
        Pattern URL_PATTERN = Pattern.compile("^.*? ?((https?:\\/\\/)?([a-zA-Z\\d][-\\w\\.]+)\\.([a-z\\.]{2,6})([-\\/\\w\\p{L}\\.~,;:%&=?+$#*]*)*\\/?) ?.*$");
        return URL_PATTERN.matcher(url).matches();
    }

    public static boolean metadataValidator(String[] versionIdsplited) {
        if (Utils.urlValidator(versionIdsplited[0]) && versionIdsplited[1].matches("[0-9]+"))
            return true;
        else
            return false;
    }
}
