package pt.arquivo.utils;

import org.archive.url.SURT;
import org.netpreserve.urlcanon.Canonicalizer;
import org.netpreserve.urlcanon.ParsedUrl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLNormalizers {

    private static final Pattern stripWWWNRuleREGEX = Pattern.compile("^(?:https?://)(?:www[0-9]*\\.)?([^/]*/?.*)$");

    public static String stripEndingSlashes(String url) {
        if (!url.isEmpty() && url.charAt(url.length() - 1) == '/') {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    public static String stripProtocolAndWWWUrl(String url) {
        Matcher matcher = stripWWWNRuleREGEX.matcher(stripEndingSlashes(url));
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            // Not http URIs fall here
            return url;
        }

    }

    public static String canocalizeUrl(String url) {
        if (url.charAt(0) == '<' && url.charAt(url.length() - 1) == '>') {
            url = url.substring(1, url.length() - 1);
        }
        ParsedUrl parsedUrl = ParsedUrl.parseUrl(stripEndingSlashes(url));
        Canonicalizer.WHATWG.canonicalize(parsedUrl);
        return parsedUrl.toString();
    }

    public static String canocalizeSurtUrl(String url) {
        Matcher matcher = stripWWWNRuleREGEX.matcher(stripEndingSlashes(url));
        if (matcher.find()) {
            return SURT.toSURT(matcher.group(1));
        } else {
            // Not http URIs fall here
            return SURT.toSURT(url);
        }
    }
}
