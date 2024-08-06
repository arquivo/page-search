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
        // Making sure the timestamp contains only digits
        for (int i = 0 ; i < timestamp.length() ; i++) {
            char c = timestamp.charAt(i);
            if (c < '0' || c > '9'){
                return "19960101000000";
            }
        }

        // YYYYMMDDhhmmss (length:14)
        String r = timestamp;
        if (timestamp.length() > 14){
            r = timestamp.substring(0, 14);
        } else if(timestamp.length() < 14) {
            r = timestamp + "19960101000000".substring(timestamp.length());
        }
        Integer year, month, day, hour, minute, second;
        
        year = Integer.parseInt(r.substring(0,4));
        month = Integer.parseInt(r.substring(4,6));
        day = Integer.parseInt(r.substring(6,8));
        hour = Integer.parseInt(r.substring(8,10));
        minute = Integer.parseInt(r.substring(10,12));
        second = Integer.parseInt(r.substring(12,14));
        if(year < 1996 || year > 9999){
            year = 1996;
        }
        if(month < 1 || month > 12){
            month = 1;
        }
        if(day < 1 || day > 31){
            day = 1;
        }
        if(hour > 23){
            hour = 0;
        }
        if(minute > 59){
            minute = 0;
        }
        if(second > 59){
            second = 0;
        }

        r = String.format("%04d", year)
        + String.format("%02d", month)
        + String.format("%02d", day)
        + String.format("%02d", hour)
        + String.format("%02d", minute)
        + String.format("%02d", second);;
        return r;
    }

    public static String timestampToSolrDate(String timestamp){
        // from YYYYMMDDhhmmss to YYYY-MM-DDThh:mm:ssZ
        String s = canocalizeTimestamp(timestamp);
        return s.substring(0,4) + "-" + s.substring(4,6) + "-" + s.substring(6,8) + "T" + s.substring(8,10) + ":" + s.substring(10,12) + ":" + s.substring(12,14) + "Z";
    }
}
