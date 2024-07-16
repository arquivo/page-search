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

    public static String canocalizeTimestamp(String timestamp){
        // YYYYMMDDhhmmss (length:14)
        String r = timestamp;
        if (timestamp.length() > 14){
            r = timestamp.substring(0, 14);
        } else if(timestamp.length() < 14) {
            r = timestamp + "19960101000000".substring(timestamp.length());
        }
        return r;
    }

    public static String timestampToSolrDate(String timestamp){
        // from YYYYMMDDhhmmss to YYYY-MM-DDThh:mm:ssZ
        String s = canocalizeTimestamp(timestamp);
        return s.substring(0,4) + "-" + s.substring(4,6) + "-" + s.substring(6,8) + "T" + s.substring(8,10) + ":" + s.substring(10,12) + ":" + s.substring(12,14) + "Z";
    }
}
