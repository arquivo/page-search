package pt.arquivo.utils;

import org.archive.url.SURT;
import org.netpreserve.urlcanon.Canonicalizer;
import org.netpreserve.urlcanon.ParsedUrl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class URLNormalizers {

    private static final Pattern stripWWWNRuleREGEX = Pattern.compile("^(?:https?://)(?:www[0-9]*\\.)?([^/]*/?.*)$");
    private static final Pattern stripProtocolRuleREGEX = Pattern.compile("^\\w+://"); 
    private static final Pattern stripWWWRuleREGEX = Pattern.compile("^www[0-9]*\\.");

    public static String stripEndingSlashes(String url) {
        if (!url.isEmpty() && url.charAt(url.length() - 1) == '/') {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    public static String stripProtocolAndWWWUrl(String url) {
        Matcher protocolMatcher = stripProtocolRuleREGEX.matcher(stripEndingSlashes(url));
        if (protocolMatcher.find()) {
            url = protocolMatcher.replaceFirst("");
        } 

        Matcher wwwMatcher = stripWWWRuleREGEX.matcher(stripEndingSlashes(url));
        if (wwwMatcher.find()) {
            url = wwwMatcher.replaceFirst("");
        } 
        
        return url;
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
            return SURT.toSURT(stripProtocolAndWWWUrl(canocalizeUrl(url)));
    }

    public static String surtToUrl(String surt) {
        int surtStartIdx,surtEndIdx;
        surtStartIdx = surt.indexOf("(");
        surtEndIdx = surt.indexOf(")");
        if(surtEndIdx < 0){ //No surt to be found here
            return canocalizeUrl(surt);
        }
        String surtPrefix = surt.substring(surtStartIdx+1, surtEndIdx);
        String surtSuffix = surt.substring(surtEndIdx+1);

        List<String> surtComponents = Arrays.asList( surtPrefix.split(",") ).stream().filter(s -> s.length() > 0 ).collect(Collectors.toList());
        Collections.reverse(surtComponents);
        return canocalizeUrl("https://"+ String.join(".", surtComponents) + surtSuffix);

    }
}
