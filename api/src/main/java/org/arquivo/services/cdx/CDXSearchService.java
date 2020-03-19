package org.arquivo.services.cdx;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.arquivo.services.SearchResult;
import org.arquivo.services.SearchResults;
import org.arquivo.services.nutchwax.NutchWaxSearchResult;
import org.springframework.beans.factory.annotation.Value;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class CDXSearchService {

    private static final Log LOG = LogFactory.getLog(CDXSearchService.class);

    private final String equalOP = "=";
    private final String andOP = "&";
    private final String outputCDX = "json";
    private final String keyUrl = "url";
    private final String keyDigest = "digest";
    private final String keyMimeType = "mime";

    @Value("${searchpages.api.globaltimeout.ms }")
    private int timeoutreadConn;

    @Value("${wayback.service.cdx.timeout}")
    private int timeoutConn;

    @Value("${wayback.service.cdx.endpoint}")
    private String waybackCdxEndpoint;

    public SearchResults getResults(String url, String from, String to, int limitP, int start) {
        Gson gson = new Gson();
        SearchResults searchResults = new SearchResults();

        ArrayList<SearchResult> results = new ArrayList<>();

        String urlCDX = generateCdxQuery(url, from, to);
        int counter = 0;
        int limit = 0;
        if (limitP > 0) {
            limit = limitP;
        }

        LOG.info("[getResults] CDX-API URL[" + urlCDX + "]");
        try {
            List<JsonObject> jsonValues = readJsonFromUrl(urlCDX);
            if (jsonValues == null)
                return null;
            if (limit > 0)
                limit = limit + start;

            for (int i = 0; i < jsonValues.size(); i++) { //convert cdx result into object
                if (counter < start) {
                    counter++;
                    continue;
                }

                SearchResult searchResult = gson.fromJson(jsonValues.get(i), NutchWaxSearchResult.class);
                results.add(searchResult);
            }
            searchResults.setResults(results);

            return searchResults;

        } catch (Exception e) {
            LOG.debug("[getResults] URL[" + urlCDX + "] e ", e);
            return null;
        }
    }

    private String generateCdxQuery(String url, String from, String to) {
        LOG.info("[CDXParser][getLink] url[" + url + "] from[" + from + "] to[" + to + "]");
        String urlEncoded = "";
        try {
            urlEncoded = URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException un) {
            LOG.error(un);
            urlEncoded = url;
        }
        LOG.info("[cdxparser] " + this.waybackCdxEndpoint);
        StringBuilder strCdxQuery = new StringBuilder();
        strCdxQuery.append(this.waybackCdxEndpoint)
                .append("?url")
                .append(equalOP)
                .append(urlEncoded)
                .append(andOP)
                .append("output")
                .append(equalOP)
                .append(outputCDX)
                .append(andOP)
                .append("from")
                .append(equalOP)
                .append(from)
                .append(andOP)
                .append("to")
                .append(equalOP)
                .append(to)
                .append(andOP)
                .append("reverse")
                .append(equalOP)
                .append("true");

        return strCdxQuery.toString();
    }


    /**
     * Connect and get response to the CDXServer
     *
     * @param strurl
     * @return
     */
    private ArrayList<JsonObject> readJsonFromUrl(String strurl) {
        InputStream is = null;
        ArrayList<JsonObject> jsonResponse = new ArrayList<JsonObject>();

        try {
            LOG.debug("[OPEN Connection]: " + strurl);
            URL url = new URL(strurl);
            URLConnection con;
            if (strurl.startsWith("https")) {
                con = (HttpsURLConnection) url.openConnection();
            } else {
                con = url.openConnection();
            }
            con.setConnectTimeout(timeoutConn);//3 sec

            // set this to a globaltimeout equal to all services
            con.setReadTimeout(timeoutreadConn);//5 sec

            is = con.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            jsonResponse = readAll(rd);
            return jsonResponse;
        } catch (Exception e) {
            LOG.error("[readJsonFromUrl]" + e);
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e1) {
                    LOG.error("[readJsonFromUrl] Close Stream: " + e1);
                }
            }
        }
    }

    /**
     * build json struture with CDXServer response
     *
     * @param rd
     * @return
     * @throws IOException
     * @throws ParseException
     */
    private ArrayList<JsonObject> readAll(BufferedReader rd) throws IOException {
        ArrayList<JsonObject> json = new ArrayList<JsonObject>();
        String line;
        while ((line = rd.readLine()) != null) {
            LOG.debug("[JSON LINE] : " + line);
            JsonParser parser = new JsonParser();
            JsonObject o = parser.parse(line.trim()).getAsJsonObject();
            json.add(o);
        }
        return json;
    }
}
