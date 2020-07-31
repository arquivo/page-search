package pt.arquivo.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    public static boolean urlValidator(String url) {
        Pattern URL_PATTERN = Pattern.compile("^.*? ?((https?:\\/\\/)?([a-zA-Z\\d][-\\w\\.]+)\\.([a-z\\.]{2,6})([-\\/\\w\\p{L}\\.~,;:%&=?+$#*]*)*\\/?) ?.*$");
        return URL_PATTERN.matcher(url).matches();
    }

    public static boolean metadataValidator(String[] versionIdsplited) {
        LOG.debug("metadata versionId[0][" + versionIdsplited[0] + "] versionId[1][" + versionIdsplited[1] + "]");
        if (Utils.urlValidator(versionIdsplited[0]) && versionIdsplited[1].matches("[0-9]+"))
            return true;
        else
            return false;
    }
}
