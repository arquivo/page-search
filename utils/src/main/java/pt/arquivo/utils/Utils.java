package pt.arquivo.utils;

import java.time.YearMonth;
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
        final String defaultTimestamp = "19910101000000";
        // Making sure the timestamp contains only digits
        for (int i = 0 ; i < timestamp.length() ; i++) {
            char c = timestamp.charAt(i);
            if (c < '0' || c > '9'){
                return defaultTimestamp;
            }
        }

        // YYYYMMDDhhmmss (length:14)
        String r = timestamp;
        if (timestamp.length() > 14){
            r = timestamp.substring(0, 14);
        } else if(timestamp.length() < 14) {
            r = timestamp + defaultTimestamp.substring(timestamp.length());
        }
        Integer year, month, day, hour, minute, second;
        
        year = Integer.parseInt(r.substring(0,4));
        month = Integer.parseInt(r.substring(4,6));
        day = Integer.parseInt(r.substring(6,8));
        hour = Integer.parseInt(r.substring(8,10));
        minute = Integer.parseInt(r.substring(10,12));
        second = Integer.parseInt(r.substring(12,14));
        if(year < 1991 || year > 9999){
            year = 1991;
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

    /**
     * Completes a timestamp, or a prefix of one, with the end of the period it names, so it can be used as an
     * inclusive upper bound: 2006 becomes 20061231235959 and 200602 becomes 20060228235959. This is how the CDX
     * server (pywb) reads a "to" date. canocalizeTimestamp completes with the start of the period instead, which is
     * right for "from".
     */
    public static String canocalizeEndTimestamp(String timestamp){
        final String endPadding = "99991231235959";
        String r = timestamp.length() > 14 ? timestamp.substring(0, 14) : timestamp;
        if (!r.matches("[0-9]*")) {
            return canocalizeTimestamp(timestamp);
        }
        r = r + endPadding.substring(r.length());

        // Padding with day 31 overshoots the shorter months, e.g. 200602 gives 20060231: use their last day instead
        int year = Integer.parseInt(r.substring(0, 4));
        int month = Integer.parseInt(r.substring(4, 6));
        int day = Integer.parseInt(r.substring(6, 8));
        if (month >= 1 && month <= 12) {
            int lastDay = YearMonth.of(year, month).lengthOfMonth();
            if (day > lastDay) {
                r = r.substring(0, 6) + String.format("%02d", lastDay) + r.substring(8);
            }
        }
        return canocalizeTimestamp(r);
    }

    public static String timestampToSolrDate(String timestamp){
        // from YYYYMMDDhhmmss to YYYY-MM-DDThh:mm:ssZ
        String s = canocalizeTimestamp(timestamp);
        return s.substring(0,4) + "-" + s.substring(4,6) + "-" + s.substring(6,8) + "T" + s.substring(8,10) + ":" + s.substring(10,12) + ":" + s.substring(12,14) + "Z";
    }
}
