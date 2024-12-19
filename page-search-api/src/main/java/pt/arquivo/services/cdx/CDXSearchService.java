package pt.arquivo.services.cdx;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import pt.arquivo.services.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class CDXSearchService {

    private static final Logger LOG = LoggerFactory.getLogger(CDXSearchService.class);

    private final String equalOP = "=";
    private final String andOP = "&";
    private final String outputCDX = "json";

    @Autowired
    private SearchService searchService;

    @Value("${searchpages.api.globaltimeout.ms}")
    private int timeoutreadConn;

    @Value("${wayback.service.cdx.timeout}")
    private int timeoutConn;

    @Value("${wayback.service.cdx.endpoint}")
    private String waybackCdxEndpoint;

    @Value("${screenshot.service.endpoint}")
    private String screenshotServiceEndpoint;

    @Value("${wayback.service.endpoint}")
    private String waybackServiceEndpoint;

    @Value("${wayback.noframe.service.endpoint}")
    private String waybackNoFrameServiceEndpoint;

    @Value("${searchpages.extractedtext.service.link}")
    private String extractedTextServiceEndpoint;

    @Value("${searchpages.textsearch.service.link}")
    private String textSearchServiceEndpoint;

    @Value("${searchpages.api.show.ids}")
    private boolean showIds;

    public SearchResults getResults(String url, String from, String to, int limitP, int start) {
        Gson gson = new Gson();
        SearchResults searchResultsResponse = new SearchResults();

        ArrayList<ItemCDX> cdxResults = new ArrayList<>();
        ArrayList<SearchResult> searchResults = new ArrayList<>();

        String urlCDX = generateCdxQuery(url, from, to);
        LOG.info("[getResults] CDX-API URL[" + urlCDX + "]");

        try {
            List<JsonObject> jsonValues = readJsonFromUrl(urlCDX);

            if (jsonValues == null) {
                LOG.error("Error while trying to get results from the CDX API.");
                searchResultsResponse.setNumberResults(0);
                searchResultsResponse.setEstimatedNumberResults(0);
                return searchResultsResponse;
            }

            // LOG.info("jsonValues Size: " + jsonValues.size());

            int limit = Math.min(jsonValues.size(), limitP + start);
            if (limit > 0) {
                for (int i = start; i < limit; i++) {
                    cdxResults.add(gson.fromJson(jsonValues.get(i), ItemCDX.class));
                }
            }

            // LOG.info("cdxResults Size: " + cdxResults.size());
            
            for (ItemCDX result : cdxResults) {

                SearchResultNutchImpl searchResult = getSearchResultNutch(result);
                searchResult.setTitle(result.getUrl());
                populateEndpointsLinks(searchResult, false);

                searchResults.add(searchResult);
                
            }
            searchResultsResponse.setResults(searchResults);
            searchResultsResponse.setEstimatedNumberResults(jsonValues.size());
            return searchResultsResponse;

        } catch (Exception e) {
            LOG.error("[getResults] URL[" + urlCDX + "] e ", e);
            searchResultsResponse.setEstimatedNumberResults(0);
            return searchResultsResponse;
        }
    }

    public static SearchResultNutchImpl getSearchResultNutch(ItemCDX result) {
        SearchResultNutchImpl searchResult = new SearchResultNutchImpl();
        searchResult.setFileName(result.getFilename());
        searchResult.setOffset(Long.parseLong(result.getOffset()));

        if (result.getLength() != null)
            searchResult.setContentLength(Long.parseLong(result.getLength()));

        searchResult.setDigest(result.getDigest());
        searchResult.setMimeType(result.getMime());
        searchResult.setTimeStamp(result.getTimestamp());
        searchResult.setOriginalURL(result.getUrl());

        if (result.getStatus() != null)
            searchResult.setStatusCode(Integer.parseInt(result.getStatus()));

        searchResult.setCollection(result.getCollection());

        return searchResult;
    }

    private String generateCdxQuery(String url, String from, String to) {
        if (from == null) {
            from = "";
        }
        if (to == null) {
            to = "";
        }

        LOG.info("[CDXParser][getLink] url[" + url + "] from[" + from + "] to[" + to + "]");
        String urlEncoded = "";
        try {
            // FIX THIS encode or escape? xD
            urlEncoded = URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException un) {
            LOG.error("Error while encoding: ", un);
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
            con.setConnectTimeout(timeoutConn);

            // set this to a globaltimeout equal to all services
            con.setReadTimeout(timeoutreadConn);

            is = con.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            jsonResponse = readAll(rd);
            LOG.info("CDX Reply: "+jsonResponse);
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


    private void populateEndpointsLinks(SearchResultNutchImpl searchResult, boolean textMatch) throws UnsupportedEncodingException {

        searchResult.setLinkToArchive(waybackServiceEndpoint.concat("/")
                .concat(searchResult.getTstamp().concat("/").concat(searchResult.getOriginalURL())));

        searchResult.setLinkToNoFrame(waybackNoFrameServiceEndpoint.concat("/")
                .concat(searchResult.getTstamp()).concat("/").concat(searchResult.getOriginalURL()));

        searchResult.setLinkToScreenshot(screenshotServiceEndpoint.concat("?url=")
                .concat(URLEncoder.encode(searchResult.getLinkToNoFrame(), StandardCharsets.UTF_8.toString())));

        searchResult.setLinkToOriginalFile(waybackNoFrameServiceEndpoint.concat("/")
                .concat(searchResult.getTstamp()).concat("id_/").concat(searchResult.getOriginalURL()));

        searchResult.setLinkToMetadata(textSearchServiceEndpoint.concat("?metadata=")
                .concat(URLEncoder.encode(searchResult.getOriginalURL().concat("/")
                        .concat(searchResult.getTstamp()), StandardCharsets.UTF_8.toString())));

        if (textMatch) {
            searchResult.setLinkToExtractedText(extractedTextServiceEndpoint.concat("?m=")
                    .concat(URLEncoder.encode(searchResult.getOriginalURL().concat("/").concat(searchResult.getTstamp()), StandardCharsets.UTF_8.toString())));
        }
    }
}
