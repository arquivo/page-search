package pt.arquivo.services.solr;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import pt.arquivo.services.*;
import pt.arquivo.utils.URLNormalizers;
import pt.arquivo.utils.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SolrSearchService implements SearchService {

    private static final Logger LOG = LoggerFactory.getLogger(SolrSearchService.class);

    // TODO should upgrade this for the SolrCloudClient
    private HttpSolrClient solrClient;

    @Value("${searchpages.api.startdate:19960101000000}")
    private String startDate;

    @Value("${searchpages.service.link:http://localhost:8081}")
    private String serviceName;

    @Value("${screenshot.service.endpoint:https://preprod.arquivo.pt/screenshot}")
    private String screenshotServiceEndpoint;

    @Value("${wayback.service.endpoint:https://preprod.arquivo.pt/wayback}")
    private String waybackServiceEndpoint;

    @Value("${wayback.noframe.service.endpoint:https://preprod.arquivo.pt/noFrame/replay}")
    private String waybackNoFrameServiceEndpoint;

    @Value("${searchpages.extractedtext.service.link:http://localhost:8081/textextracted}")
    private String extractedTextServiceEndpoint;

    @Value("${searchpages.textsearch.service.bean.solr.link:http://localhost:8983/solr/searchpages}")
    private String baseSolrUrl;

    @Value("${searchpages.textsearch.service.link:http://localhost:8081/textsearch}")
    private String textSearchServiceEndpoint;

    private String[] allRelevantRequestFields = null;

    public HttpSolrClient getSolrClient() {
        if (this.solrClient == null) {
            LOG.info("Initing SolrClient pointing to " + this.baseSolrUrl);
            this.solrClient = new HttpSolrClient.Builder(this.baseSolrUrl).build();
        }
        return this.solrClient;
    }

    // new Hashtable<String, Boolean>();

    private String[] getDefaultRequestFields(){
        if (allRelevantRequestFields == null){ 
            ArrayList<String> fields = new ArrayList<String>();
            fields.add("id");
            fields.add("type");
            // fields.add("typeReported");
            fields.add("tstamp");
            // fields.add("date");
            // fields.add("dateLatest");
            // fields.add("timeRange");
            // fields.add("host");
            fields.add("urlTimestamp");
            // fields.add("surts");
            // fields.add("inlinksInternal");
            // fields.add("inlinksExternal");
            // fields.add("captureCount");
            fields.add("statusCode");
            // fields.add("metadata");
            fields.add("title");
            fields.add("collection");
            // fields.add("inlinkAnchorsInternal");
            // fields.add("urlTokens");
            // fields.add("urls");
            // fields.add("_version_");
            // fields.add("content");
            allRelevantRequestFields = fields.toArray(new String[0]);
         }
         return allRelevantRequestFields;
    }

    // TODO refactor this - extract duplicate code
    private void populateEndpointsLinks(SearchResultSolrImpl searchResult, String oldestTimestamp, String oldestUrl) throws UnsupportedEncodingException {
        searchResult.setLinkToArchive(waybackServiceEndpoint +
                "/" + oldestTimestamp +
                "/" + oldestUrl);

        searchResult.setLinkToNoFrame(waybackNoFrameServiceEndpoint +
                "/" + oldestTimestamp +
                "/" + oldestUrl);

        searchResult.setLinkToScreenshot(screenshotServiceEndpoint +
                "?url=" + URLEncoder.encode(searchResult.getLinkToNoFrame(), StandardCharsets.UTF_8.toString()));

        searchResult.setLinkToExtractedText(extractedTextServiceEndpoint.concat("?m=")
                .concat(URLEncoder.encode(oldestUrl.concat("/").concat(oldestTimestamp), StandardCharsets.UTF_8.toString())));

        searchResult.setLinkToMetadata(textSearchServiceEndpoint.concat("?metadata=")
                .concat(URLEncoder.encode(oldestUrl.concat("/").concat(oldestTimestamp), StandardCharsets.UTF_8.toString())));

        searchResult.setLinkToOriginalFile(waybackNoFrameServiceEndpoint +
                "/" + oldestTimestamp +
                "id_/" + oldestUrl);
    }

    private void addDeduplicationFilterQuery(SolrQuery solrQuery, String dedupField) {
        // if (dedupField.equalsIgnoreCase("url")) {
        //     dedupField = "surt_url";
        // }

        // StringBuilder stringBuilder = new StringBuilder();
        // stringBuilder.append("{!collapse field=")
        //         .append(dedupField).append("}");
        // solrQuery.addFilterQuery(stringBuilder.toString());
    }

    private SolrQuery convertSearchQuery(SearchQuery searchQuery) {
        SolrQuery solrQuery = new SolrQuery();

        solrQuery.setQuery(ClientUtils.escapeQueryChars(searchQuery.getQueryTerms()));
        solrQuery.add("df","content");   // Remove this when solr has a default df
        solrQuery.setStart(searchQuery.getOffset()); // No need to escape because offset and maxItems are integers
        solrQuery.setRows(searchQuery.getMaxItems());

        // Enable highlighting
        solrQuery.setHighlight(true);

        if (searchQuery.isSearchByCollection()) {
            boolean multipleCollection = false;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("collection:");
            for (String collection : searchQuery.getCollection()) {
                if (multipleCollection) stringBuilder.append(" OR ");
                stringBuilder.append(ClientUtils.escapeQueryChars(collection));
                multipleCollection = true;
            }
            solrQuery.addFilterQuery(stringBuilder.toString());
        }

        if (searchQuery.isTimeBoundedQuery()) {
            if (searchQuery.getFrom() != null){
                solrQuery.addFilterQuery("dateLatest:[ "+ClientUtils.escapeQueryChars(Utils.timestampToSolrDate(searchQuery.getFrom()))+" TO * ]");
            }
            if (searchQuery.getTo() != null){
                solrQuery.addFilterQuery("date:[ * TO "+ClientUtils.escapeQueryChars(Utils.timestampToSolrDate(searchQuery.getTo()))+"]");
            }
        }

        if (searchQuery.isSearchBySite()) {
            String[] sites = searchQuery.getSite();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("surts:");
            boolean multipleSite = false;
            for (String site : sites) {
                if (multipleSite) stringBuilder.append(" OR surts:");
                
                String[] fullSurt = URLNormalizers.canocalizeSurtUrl(site).split("\\)");
                if(fullSurt.length == 1 || fullSurt[1].length() == 0 ){
                    // If only a domain is given, search for all subdomains
                    stringBuilder.append(ClientUtils.escapeQueryChars(fullSurt[0]) + "*");
                } else {
                    // If a full URL is given, look only for the exact URL 
                    stringBuilder.append(ClientUtils.escapeQueryChars(fullSurt[0] + ")" + fullSurt[1]) + "*");
                }
                multipleSite = true;
            }
            solrQuery.addFilterQuery(stringBuilder.toString());
        }

        // filter fields
        String[] fieldsArray = getDefaultRequestFields(); 
        Boolean[] fieldsToInclude = new Boolean[fieldsArray.length];
        Boolean needsSnippet = true; //Snippet is more complex, we'll handle it separately
        if (searchQuery.getFields() != null) {
            Hashtable<String,Integer> fieldsIndexes = new Hashtable<String,Integer>();
            for(int i = 0; i< fieldsArray.length; i++){
                fieldsIndexes.put(fieldsArray[i],i);
                fieldsToInclude[i] = false;
            }
            fieldsToInclude[fieldsIndexes.get("urlTimestamp")] = true;
            needsSnippet = false;
            for(String field: searchQuery.getFields()){
                switch (field) {
                    case "title":
                    case "tstamp":
                    case "collection":
                        fieldsToInclude[fieldsIndexes.get(field)] = true;
                        break;
                    case "digest":
                    case "id":
                        fieldsToInclude[fieldsIndexes.get("id")] = true;
                        break;
                    case "mimeType":
                        fieldsToInclude[fieldsIndexes.get("type")] = true;
                        break;
                    case "snippet":
                        fieldsToInclude[fieldsIndexes.get("id")] = true;
                        needsSnippet = true;
                        break;
                }
            }
            // Solr fields: id, type, typeReported, tstamp, urlTimestamp, statusCode, title, collection, 
            // API fields: title, originalURL, linkToArchive, tstamp, contentLength, digest, mimeType, linkToScreenshot, date, encoding, linkToNoFrame, linkToOriginalFile, collection, snippet, linkToExtractedText
        } else {
            for(int i = 0; i< fieldsArray.length; i++){
                fieldsToInclude[i] = true;
            }
        }
        StringBuilder stringBuilderFields = new StringBuilder();
            boolean multipleFields = false;
            for (int i = 0; i < fieldsArray.length; i++) {
                if(fieldsToInclude[i] == true){
                    if(multipleFields){ 
                        stringBuilderFields.append(","); 
                    }
                    stringBuilderFields.append(fieldsArray[i]);
                    multipleFields = true;
                }
            }
            solrQuery.setFields(stringBuilderFields.toString());

        if (needsSnippet) {
            solrQuery.setHighlight(true);
            solrQuery.setHighlightFragsize(50);
            solrQuery.setHighlightSnippets(10);
            solrQuery.add("hl.method","fastVector");
        }

        addDeduplicationFilterQuery(solrQuery, searchQuery.getDedupField());

        return solrQuery;
    }

    public String getHighlightedText(final QueryResponse queryResponse, final String fieldName, final String docId) {
        String highlightedText = "";
        Map<String, Map<String, List<String>>> highlights = queryResponse.getHighlighting();

        Map<String, List<String>> fieldsSnippet;
        fieldsSnippet = highlights.getOrDefault(docId, null);

        if (fieldsSnippet != null) {
            List<String> snippets = fieldsSnippet.getOrDefault(fieldName, null);
            if (snippets != null) {
                highlightedText = getFragments(snippets);
            }
        }
        
        return highlightedText;
    }

    private static final String getFragments(List<String> snippets) {
        StringBuilder fragments = new StringBuilder();
        for (int i = 0; i < snippets.size(); i++) {
            if (i > 0) {
                fragments.append("<span class=\"ellipsis\"> ... </span>");
            }
            fragments.append(snippets.get(i));
        }
        return fragments.toString();
    }


    private Object coalesce(Object obj1, Object obj2){
        if(obj1 == null){
            return obj2;
        }
        return obj1;
    }

    @SuppressWarnings("unchecked")
    private Object getFirstResult(SolrDocument doc, String fieldName, Object defaultValue){
        Collection <Object> solrField = (Collection <Object>) coalesce(doc.getFieldValues(fieldName), new HashSet<Object> ());
        if(solrField.size() == 0){
            return defaultValue;
        }
        return solrField.iterator().next();
    }

    private String timestampSurtToTimestamp(String tu){
        return tu.substring(0,tu.indexOf("/"));
    }
    private String timestampSurtToSurt(String tu){
        return tu.substring(tu.indexOf("/")+1);
    }

    private SearchResults parseQueryResponse(QueryResponse queryResponse, SearchQuery searchQuery) {
        SearchResults searchResults = new SearchResults();
        ArrayList<SearchResult> searchResultArrayList = new ArrayList<>();

        SolrDocumentList solrDocumentList = queryResponse.getResults();

        final Long to,from;
        final String[] siteSearchSurts;

        if(searchQuery.isSearchBySite()){
            siteSearchSurts = Arrays.asList(searchQuery.getSite()).stream()
                .map(s -> URLNormalizers.canocalizeSurtUrl(s))
                .collect(Collectors.toList())
                .toArray(new String[0]);
        } else {
            siteSearchSurts = null;
        }

        if (searchQuery.getFrom() != null){
            from = Long.parseLong(Utils.canocalizeTimestamp(searchQuery.getFrom()));
        } else {
            from = null;
        }

        if (searchQuery.getTo() != null){
            to = Long.parseLong(Utils.canocalizeTimestamp(searchQuery.getTo()));
        } else {
            to = null;
        }

        for (SolrDocument doc : solrDocumentList) {
            
            List<Object> urlstimestamps = new ArrayList<Object>(doc.getFieldValues("urlTimestamp"));
            
            // Sanity check: urltimestamps should be in format TIMESTAMP/SURT
            urlstimestamps = urlstimestamps.stream()
                .filter(u -> ((String) u).indexOf("/") >= 0)
                .collect(Collectors.toList());

            // Filter out urltimestamps from wrong URLs if siteSearch was made
            if(siteSearchSurts != null){
                urlstimestamps = urlstimestamps.stream()
                .filter(tu -> StringUtils.startsWithAny(timestampSurtToSurt((String) tu), siteSearchSurts))
                .collect(Collectors.toList());
            }

            // Filter out urltimestamps from outside time-range if it's a time bounded query
            if (from != null){
                urlstimestamps = urlstimestamps.stream()
                .filter(u -> Long.parseLong(timestampSurtToTimestamp((String) u)) >= from)
                .collect(Collectors.toList());
            }
            if (to != null){
                urlstimestamps = urlstimestamps.stream()
                .filter(u -> Long.parseLong(timestampSurtToTimestamp((String) u)) <= to)
                .collect(Collectors.toList());
            }

            if(urlstimestamps.size() == 0){
                continue;
            }

            // Find oldest URL/timestamp that matches the user query
            String oldestUrl = null, oldestTimestamp = null;

            Iterator <Object> it = (Iterator <Object>) urlstimestamps.iterator();
            while (it.hasNext()){
                String currentUrlTimestamp = (String) it.next();
                String currentTimestamp = timestampSurtToTimestamp(currentUrlTimestamp);

                if(oldestUrl == null || Long.parseLong(oldestTimestamp) > Long.parseLong(currentTimestamp)) {
                    oldestTimestamp = currentTimestamp;
                    oldestUrl = URLNormalizers.surtToUrl(timestampSurtToSurt(currentUrlTimestamp));
                }
            }

            SearchResultSolrImpl searchResult = new SearchResultSolrImpl();
            searchResult.setTitle((String) doc.getFieldValue("title"));
            searchResult.setOriginalURL(oldestUrl);
            searchResult.setMimeType((String) getFirstResult(doc,"type",""));
            searchResult.setTstamp(Long.parseLong(oldestTimestamp));
            // searchResult.setOffset((Long) doc.getFieldValue("warc_offset"));
            // searchResult.setFileName((String) doc.getFieldValue("warc_name"));
            searchResult.setStatusCode((Integer) coalesce(doc.getFieldValue("statusCode"), Integer.valueOf(0)));
            searchResult.setCollection((String) getFirstResult(doc,"collection",""));
            searchResult.setContentLength(Long.valueOf(((String) getFirstResult(doc,"content","")).length()));
            searchResult.setDigest((String) doc.getFieldValue("id"));
            // searchResult.setEncoding((String) doc.getFieldValue("encoding"));
            searchResult.setId((String) coalesce(doc.getFieldValue("id"),""));
            searchResult.setSolrClient(this.solrClient);

            searchResult.setSnippet(getHighlightedText(queryResponse, "content", (String) doc.get("id")));
            try {
                populateEndpointsLinks(searchResult,oldestTimestamp,oldestUrl);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            searchResultArrayList.add(searchResult);
        }
        searchResults.setResults(searchResultArrayList);
        searchResults.setEstimatedNumberResults(queryResponse.getResults().getNumFound());
        searchResults.setNumberResults(queryResponse.getResults().size());

        return searchResults;
    }

    @Override
    public SearchResults query(SearchQuery searchQuery, boolean urlSearch) {
        if (urlSearch) {
            String queryTerms = searchQuery.getQueryTerms();
            List<String> solrQueryForSites = Arrays.asList( queryTerms.split(",") ).stream()
                .filter(url -> Utils.urlValidator(url))
                .map(url -> URLNormalizers.canocalizeSurtUrl(url))
                .map(surt -> "surts:" + ClientUtils.escapeQueryChars(surt) + "*" )
                .collect(Collectors.toList());
            searchQuery.setQueryTerms(String.join(" OR ", solrQueryForSites));
        }
        return query(searchQuery);
    }

    @Override
    public SearchResults query(SearchQuery searchQuery) {
        SolrQuery solrQuery = convertSearchQuery(searchQuery);
        try {
            QueryResponse queryResponse = this.getSolrClient().query(solrQuery);
            SearchResults searchResults = parseQueryResponse(queryResponse,searchQuery);
            searchResults.setLastPageResults(isLastPage(searchResults.getEstimatedNumberResults(), searchQuery));
            return searchResults;
        } catch (SolrServerException | IOException e) {
            LOG.error("Error querying Solr: ", e);
        }

        SearchResults searchResults = new SearchResults();
        searchResults.setEstimatedNumberResults(0);
        searchResults.setLastPageResults(false);
        searchResults.setNumberResults(0);
        return searchResults;
    }

    public static boolean isLastPage(long numberOfResults, SearchQuery searchQuery) {
        return numberOfResults <= searchQuery.getOffset() + searchQuery.getMaxItems();
    }

    public String getScreenshotServiceEndpoint() {
        return screenshotServiceEndpoint;
    }

    public String getWaybackServiceEndpoint() {
        return waybackServiceEndpoint;
    }

    public String getWaybackNoFrameServiceEndpoint() {
        return waybackNoFrameServiceEndpoint;
    }

    public String getExtractedTextServiceEndpoint() {
        return extractedTextServiceEndpoint;
    }

    public String getTextSearchServiceEndpoint() {
        return textSearchServiceEndpoint;
    }
}
