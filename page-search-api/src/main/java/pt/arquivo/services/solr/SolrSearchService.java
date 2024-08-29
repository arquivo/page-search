package pt.arquivo.services.solr;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import pt.arquivo.services.SearchQuery;
import pt.arquivo.services.SearchResult;
import pt.arquivo.services.SearchResultSolrImpl;
import pt.arquivo.services.SearchResults;
import pt.arquivo.services.SearchService;
import pt.arquivo.services.SearchServiceConfiguration;
import pt.arquivo.utils.URLNormalizers;
import pt.arquivo.utils.Utils;

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

    // For setting configs without autowired shanenigans
    public SolrSearchService(SearchServiceConfiguration configuration){
        this.startDate = configuration.getStartDate();
        this.serviceName = configuration.getServiceName();
        this.screenshotServiceEndpoint = configuration.getScreenshotServiceEndpoint();
        this.waybackServiceEndpoint = configuration.getWaybackServiceEndpoint();
        this.waybackNoFrameServiceEndpoint = configuration.getWaybackNoFrameServiceEndpoint();
        this.extractedTextServiceEndpoint = configuration.getExtractedTextServiceEndpoint();
        this.baseSolrUrl = configuration.getBaseSolrUrl();
        this.textSearchServiceEndpoint = configuration.getTextSearchServiceEndpoint();
    }

    public SolrSearchService(){}

    public HttpSolrClient getSolrClient() {
        if (this.solrClient == null) {
            LOG.info("Initing SolrClient pointing to " + this.baseSolrUrl);
            this.solrClient = new HttpSolrClient.Builder(this.baseSolrUrl).build();
        }
        return this.solrClient;
    }

    /**
     * Makes sure that the requested dedupField is a valid solr field. When the dedup field is "site" it gets converted into 
     * "surt", and if it's "mimetype" it gets converted into "type" (because those are the Solr field names). When invalid
     * or empty, the dedup field defaults to "surt" (which is the same as "site"). 
     * @param dedupField
     * @return
     */
    private String sanitizeDedupField(String dedupField){
        if (dedupField == null) { 
            dedupField = "";
        }
        dedupField = dedupField.toLowerCase();

        final List<String> validDedupFields = Arrays.asList(new String[] {"site","surt", "surtOldest", "mimetype","type","collection","collectionOldest","title","titleString"});

        // By default dedup by title, if invalid dedupField then fallback to dedup by title
        if(!validDedupFields.contains(dedupField) || dedupField.equals("title") ){
            dedupField = "titleString";
        } else if (dedupField.equals("site") || dedupField.equals("surt")){
            dedupField = "surtOldest";
        } else if(dedupField.equals("mimetype")){
            dedupField = "type";
        }

        return dedupField;
    }  
    
    /**
     * Converts the API request into an appropriate Solr query.
     * @param searchQuery
     * @return
     */
    private SolrQuery convertSearchQuery(SearchQuery searchQuery) {
        SolrQuery solrQuery = new SolrQuery();

        solrQuery.setQuery(ClientUtils.escapeQueryChars(searchQuery.getQueryTerms()));
        solrQuery.setStart(searchQuery.getOffset()); // No need to escape because offset and maxItems are integers
        solrQuery.setRows(searchQuery.getMaxItems());

        // Handle collection request:
        if (searchQuery.isSearchByCollection()) {
            boolean multipleCollection = false;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("collections:");
            for (String collection : searchQuery.getCollection()) {
                if (multipleCollection)
                    stringBuilder.append(" OR ");
                stringBuilder.append(ClientUtils.escapeQueryChars(collection));
                multipleCollection = true;
            }
            solrQuery.addFilterQuery(stringBuilder.toString());
        }

        // Handle to/from request
        if (searchQuery.isTimeBoundedQuery()) {
            if (searchQuery.getFrom() != null) {
                solrQuery.addFilterQuery("dateLatest:[ "
                        + ClientUtils.escapeQueryChars(Utils.timestampToSolrDate(searchQuery.getFrom())) + " TO * ]");
            }
            if (searchQuery.getTo() != null) {
                solrQuery.addFilterQuery("dateOldest:[ * TO "
                        + ClientUtils.escapeQueryChars(Utils.timestampToSolrDate(searchQuery.getTo())) + "]");
            }
        }

        // Handle site request
        if (searchQuery.isSearchBySite()) {
            String[] sites = searchQuery.getSite();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("surts:");
            boolean multipleSite = false;
            for (String site : sites) {
                if (multipleSite)
                    stringBuilder.append(" OR surts:");

                String[] fullSurt = URLNormalizers.canocalizeSurtUrl(site).split("\\)");
                if (fullSurt.length == 1 || fullSurt[1].length() == 0) {
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

        // Handle deduplication:
        if (searchQuery.getDedupValue() >= 0){
            Integer dedupValue = searchQuery.getDedupValue();
            String dedupField = sanitizeDedupField(searchQuery.getDedupField());

            if(dedupValue > 0){ 
                dedupValue -= 1;
            }

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{!collapse field=")
                            .append(dedupField)
                            .append("}");
            solrQuery.addFilterQuery(stringBuilder.toString());
            if(dedupValue > 0){
                solrQuery.add("expand", "true");
                solrQuery.add("expand.rows", dedupValue.toString());
            }
        } 

        // Handle type request
        if (searchQuery.isSearchByType()) {
            String[] types = searchQuery.getType();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("type:");
            boolean multipleType = false;
            for (String type : types) {
                if (multipleType)
                    stringBuilder.append(" OR type:");

                type = type.toLowerCase();
                if (type.indexOf('/')<0){
                    switch (type) {
                        case "pdf":
                            stringBuilder.append(ClientUtils.escapeQueryChars("application/pdf"));
                            break;
                        case "xls":
                        case "xlsx":
                            stringBuilder.append(ClientUtils.escapeQueryChars("application/vnd.ms-excel"));
                            stringBuilder.append(" OR type:");
                            stringBuilder.append(ClientUtils.escapeQueryChars("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
                            break;
                        case "ppt":
                        case "pptx":
                            stringBuilder.append(ClientUtils.escapeQueryChars("application/vnd.ms-powerpoint"));
                            stringBuilder.append(" OR type:");
                            stringBuilder.append(ClientUtils.escapeQueryChars("application/vnd.openxmlformats-officedocument.presentationml.presentation"));
                            break;
                        case "doc":
                        case "docx":
                            stringBuilder.append(ClientUtils.escapeQueryChars("application/msword"));
                            stringBuilder.append(" OR type:");
                            stringBuilder.append(ClientUtils.escapeQueryChars("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
                            break;
                        case "ps":
                        case "ai":
                        case "eps":
                            stringBuilder.append(ClientUtils.escapeQueryChars("application/postscript"));
                            break;
                        case "htm":
                        case "html":
                            stringBuilder.append(ClientUtils.escapeQueryChars("text/html"));
                            break;
                        case "rtf":
                            stringBuilder.append(ClientUtils.escapeQueryChars("application/rtf"));
                            break;
                        default:
                            stringBuilder.append("*" + ClientUtils.escapeQueryChars(type) + "*");
                            break;
                    }
                } else {
                    stringBuilder.append(ClientUtils.escapeQueryChars(type));
                }

                multipleType = true;
            }
            solrQuery.addFilterQuery(stringBuilder.toString());
        }


        // Optimization: Make sure we only ask the fields we need.
        // At most we'll only need these fields from Solr:
        String[] fieldsArray = new String[] { "id", "type", "urlTimestamp", "title" };
        Hashtable<String, Boolean> fieldInclusivity = new Hashtable<String, Boolean>();
        Boolean needsSnippet = true; // Snippet is different, we'll handle it separately

        // If user only asks for certain fields, we don't need to ask for every field
        if (searchQuery.getFields() != null) {
            for (int i = 0; i < fieldsArray.length; i++) {
                fieldInclusivity.put(fieldsArray[i], false);
            }

            // We always need urlTimestamp, it's where we get the collection, url and timestamp
            fieldInclusivity.put("urlTimestamp", true);


            // If we're deduping we'll need to get the dedup field to get the expand from solr 
            if (searchQuery.getDedupValue() > 1){
                String dedupField = sanitizeDedupField(searchQuery.getDedupField());
                fieldInclusivity.put(dedupField, true);
            }

            needsSnippet = false;
            for (String field : searchQuery.getFields()) {
                switch (field) {
                    case "title":
                        fieldInclusivity.put("title", true);
                        break;
                    case "mimeType":
                        fieldInclusivity.put("type", true);
                        break;
                    case "snippet":
                        fieldInclusivity.put("id", true);
                        needsSnippet = true;
                        break;
                }
            }
        } else {
            for (int i = 0; i < fieldsArray.length; i++) {
                fieldInclusivity.put(fieldsArray[i], true);
            }
        }
        StringBuilder stringBuilderFields = new StringBuilder();
        boolean multipleFields = false;
        for (int i = 0; i < fieldsArray.length; i++) {
            if (fieldInclusivity.get(fieldsArray[i]) == true) {
                if (multipleFields) {
                    stringBuilderFields.append(",");
                }
                stringBuilderFields.append(fieldsArray[i]);
                multipleFields = true;
            }
        }

        solrQuery.setFields(stringBuilderFields.toString());

        // If we don't need snippet we don't ask Solr for highligting (which is on by default since v5)
        if(!needsSnippet){
            solrQuery.set("hl","false");
        }

        LOG.info("Solr Query: "+solrQuery);
        return solrQuery;
    }

    /**
     * Gets the highlighted text from the Solr "content" field to fill the API "snippet" field. If there was no text to be 
     * hightlighted in the Solr "content" field, will use the first 500 chars of the "content" field instead.
     * 
     * @param queryResponse
     * @param fieldName
     * @param docId
     * @return
     */
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

        // If we don't get highlighted text on the content we display the first 500 chars of the content
        if (highlightedText.length() == 0) {
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.set("q", "id:" + docId);
            solrQuery.set("fl", "content");
            solrQuery.set("hl","false");
            try {
                SolrDocumentList solrDocumentList = getSolrClient().query(solrQuery).getResults();
                if (solrDocumentList.size() > 0) {
                    String content = (String) solrDocumentList.get(0).getFieldValue("content");
                    if (content != null && content.length() > 0) {
                        if (content.length() <= 500) {
                            highlightedText = content;
                        } else {
                            highlightedText = content.substring(0, 500) + "<span class=\"ellipsis\"> ... </span>";
                        }
                    }
                }
            } catch (SolrServerException | IOException e) {
                LOG.error("Error querying Solr: ", e);
            }
        }
        return highlightedText;
    }

    private static final String getFragments(List<String> snippets) {
        StringBuilder fragments = new StringBuilder();
        for (int i = 0; i < snippets.size(); i++) {
            fragments.append(snippets.get(i));
            fragments.append("<span class=\"ellipsis\"> ... </span>");
        }
        return fragments.toString();
    }

    /**
     * If obj1 is not null returns obj1. Otherwise returns obj2.
     *
     * @param obj1
     * @param obj2
     * @return Object
     */
    private Object coalesce(Object obj1, Object obj2) {
        if (obj1 == null) {
            return obj2;
        }
        return obj1;
    }

    /**
     * Extracts the collection from a string in the format collection/timestamp/surt (as returned by the Solr field urlTimestamp)
     *
     * @param tu
     * @return String
     */
    private String timestampSurtToCollection(String tu) {
        return tu.substring(0, tu.indexOf("/"));
    }

    /**
     * Extracts the timestamp from a string in the format collection/timestamp/surt (as returned by the Solr field urlTimestamp)
     *
     * @param tu
     * @return String
     */
    private String timestampSurtToTimestamp(String tu) {
        String r = tu.substring(tu.indexOf("/") + 1);
        return r.substring(0, r.indexOf("/"));
    }

    /**
     * Extracts the surt from a string in the format collection/timestamp/surt (as returned by the Solr field urlTimestamp)
     *
     * @param tu
     * @return String
     */
    private String timestampSurtToSurt(String tu) {
        String r = tu.substring(tu.indexOf("/") + 1);
        return r.substring(r.indexOf("/") + 1);
    }

    /**
     * Given a list of strings in the format collection/timestamp/surt (as returned by the Solr field urlTimestamp),
     * will filter out entries that do not match the user query, i.e., timestamp outside to/from range, surt doesn't match
     * siteSearch or collection doesn't match collection search
     *
     */
    private List<Object> filterUrlTimestamps(List <Object> urlstimestamps, Long to, Long from, String[] siteSearchSurts, String[] collectionSearch){
                urlstimestamps = urlstimestamps.stream()
                        .filter(u -> ((String) u).indexOf("/") >= 0)
                        .collect(Collectors.toList());

                // Filter out urltimestamps from wrong URLs if siteSearch was made
                if (siteSearchSurts != null) {
                    urlstimestamps = urlstimestamps.stream()
                            .filter(tu -> StringUtils.startsWithAny(timestampSurtToSurt((String) tu), siteSearchSurts))
                            .collect(Collectors.toList());
                }

                // Filter out urltimestamps from outside time-range if it's a time bounded query
                if (from != null) {
                    urlstimestamps = urlstimestamps.stream()
                            .filter(u -> Long.parseLong(timestampSurtToTimestamp((String) u)) >= from)
                            .collect(Collectors.toList());
                }
                if (to != null) {
                    urlstimestamps = urlstimestamps.stream()
                            .filter(u -> Long.parseLong(timestampSurtToTimestamp((String) u)) <= to)
                            .collect(Collectors.toList());
                }

                // Filter out collections if it's a collection bounded search
                if (collectionSearch != null) {
                    urlstimestamps = urlstimestamps.stream()
                            .filter(tu -> Arrays.asList(collectionSearch)
                                    .contains(timestampSurtToCollection((String) tu)))
                            .collect(Collectors.toList());
                }

                return urlstimestamps;
    }

    /**
     * Given a list of strings in the format collection/timestamp/surt (as returned by the Solr field urlTimestamp), will
     * return the one with the oldest timestamp
     */
    private String getOldestUrlTimestamp(List <Object> urlstimestamps){
        String oldestTimestamp = null;
        String oldestUrlTimestamp = null;
        Iterator<Object> it = (Iterator<Object>) urlstimestamps.iterator();
        while (it.hasNext()) {
            String currentUrlTimestamp = (String) it.next();
            String currentTimestamp = timestampSurtToTimestamp(currentUrlTimestamp);

            if (oldestTimestamp == null || Long.parseLong(oldestTimestamp) > Long.parseLong(currentTimestamp)) {
                oldestTimestamp = currentTimestamp;
                oldestUrlTimestamp = currentUrlTimestamp;
            }
        }
        return oldestUrlTimestamp;
    }

    /**
     * Takes a SolrDocument object and converts it into a SearchResult, making sure the result matches the user query.  
     * Will return null if the document doesn't match the user query.
     * 
     * @param doc
     * @param queryResponse
     * @param to
     * @param from
     * @param siteSearchSurts
     * @param collectionSearch
     * @param replyFields
     * @return
     */
    private SearchResultSolrImpl getSearchResultfromSolrDocument(SolrDocument doc, QueryResponse queryResponse, Long to, Long from, String[] siteSearchSurts, String[] collectionSearch, String[] replyFields ){
        String oldestUrl = null;
        String oldestTimestamp = null;
        String oldestCollection = null;

        
        List<Object> urlstimestamps = new ArrayList<Object>(doc.getFieldValues("urlTimestamp"));
        urlstimestamps = filterUrlTimestamps(urlstimestamps, to, from, siteSearchSurts, collectionSearch);

        // Sometimes after filtering we get no results
        if (urlstimestamps.size() == 0) {
            return null;
        }

        String oldestUrlTimestamp = getOldestUrlTimestamp(urlstimestamps);

        oldestTimestamp = timestampSurtToTimestamp(oldestUrlTimestamp);
        oldestUrl = URLNormalizers.surtToUrl(timestampSurtToSurt(oldestUrlTimestamp));
        oldestCollection = timestampSurtToCollection(oldestUrlTimestamp);
        

        SearchResultSolrImpl searchResult = new SearchResultSolrImpl();
        populateSearchResult(searchResult, queryResponse, doc, oldestUrl, oldestTimestamp, oldestCollection, replyFields);
        searchResult.setSolrClient(this.solrClient);
        return searchResult;
    }

    /**
     * Parses the reply from Solr and populates the API search results, making sure every result mateches the user query.
     * 
     * @param queryResponse
     * @param searchQuery
     * @return
     */
    private SearchResults parseQueryResponse(QueryResponse queryResponse, SearchQuery searchQuery) {
        SearchResults searchResults = new SearchResults();
        ArrayList<SearchResult> searchResultArrayList = new ArrayList<>();

        SolrDocumentList solrDocumentList = queryResponse.getResults();

        final Long to, from;
        final String[] siteSearchSurts;
        final String[] collectionSearch;
        final String[] replyFields;
        final Map<String, SolrDocumentList> expandedResults = queryResponse.getExpandedResults();

        // Check which fields the user asked for
        if (searchQuery.getFields() == null) {
            // Default reply fields
            replyFields = new String[] { "title", "originalURL", "mimeType", "tstamp", "digest", "collection", "id",
                    "snippet", "linkToArchive", "linkToNoFrame", "linkToScreenshot", "linkToExtractedText",
                    "linkToMetadata", "linkToOriginalFile" };
        } else {
            replyFields = searchQuery.getFields();
        }

        // Check if the user searched for one or more specific websites
        if (searchQuery.isSearchBySite()) {
            siteSearchSurts = Arrays.asList(searchQuery.getSite()).stream()
                    .map(s -> URLNormalizers.canocalizeSurtUrl(s))
                    .collect(Collectors.toList())
                    .toArray(new String[0]);
        } else {
            siteSearchSurts = null;
        }

        // Check if the request is time-bounded 
        if (searchQuery.getFrom() != null) {
            from = Long.parseLong(Utils.canocalizeTimestamp(searchQuery.getFrom()));
        } else {
            from = null;
        }
        if (searchQuery.getTo() != null) {
            to = Long.parseLong(Utils.canocalizeTimestamp(searchQuery.getTo()));
        } else {
            to = null;
        }

        // Check if the user serched for one or more specific collections
        if (searchQuery.isSearchByCollection()) {
            collectionSearch = searchQuery.getCollection();
        } else {
            collectionSearch = null;
        }

        for (SolrDocument doc : solrDocumentList) {

            SearchResultSolrImpl searchResult = getSearchResultfromSolrDocument(doc,queryResponse,to,from,siteSearchSurts,collectionSearch,replyFields);
            if(searchResult == null){
                continue;
            }
            searchResultArrayList.add(searchResult);
            
            if (expandedResults != null && expandedResults.size() > 0) {
                
                String dedupField = sanitizeDedupField(searchQuery.getDedupField());
                String expandedDedupValue = (String) doc.getFieldValue(dedupField);
                if (expandedDedupValue == null || !expandedResults.containsKey(expandedDedupValue)) {
                    continue;
                }

                Iterator<SolrDocument> expandedDocumentIterator = expandedResults.get(expandedDedupValue).iterator();
                while(expandedDocumentIterator.hasNext()){
                    SolrDocument expandedDoc = expandedDocumentIterator.next();

                    SearchResultSolrImpl expandedResult = getSearchResultfromSolrDocument(expandedDoc,queryResponse,to,from,siteSearchSurts,collectionSearch,replyFields);
                    if(expandedResult == null){
                        continue;
                    }
                    searchResultArrayList.add(expandedResult);
                }
            }

        }
        searchResults.setResults(searchResultArrayList);
        searchResults.setEstimatedNumberResults(queryResponse.getResults().getNumFound());
        searchResults.setNumberResults(queryResponse.getResults().size());

        return searchResults;
    }

    /**
     * Populates one API search result, making sure only to populate fields that the user asked for.
     * 
     * @param searchResult
     * @param queryResponse
     * @param doc
     * @param oldestUrl
     * @param oldestTimestamp
     * @param oldestCollection
     * @param replyFields
     */
    private void populateSearchResult(SearchResultSolrImpl searchResult, QueryResponse queryResponse, SolrDocument doc,
            String oldestUrl, String oldestTimestamp, String oldestCollection, String[] replyFields) {
        for (String field : replyFields) {
            switch (field) {
                case "title":
                    searchResult.setTitle((String) coalesce(doc.getFieldValue("title"), ""));
                    break;
                case "originalURL":
                    searchResult.setOriginalURL(oldestUrl);
                    break;
                case "mimeType":
                    searchResult.setMimeType((String) coalesce(doc.getFieldValue("type"), ""));
                    break;
                case "tstamp":
                    searchResult.setTstamp(Long.parseLong(oldestTimestamp));
                    break;
                case "digest":
                    searchResult.setDigest((String) coalesce(doc.getFieldValue("id"), ""));
                    break;
                case "collection":
                    searchResult.setCollection(oldestCollection);
                    break;
                case "id":
                    searchResult.setId((String) coalesce(doc.getFieldValue("id"), ""));
                    break;
                case "snippet":
                    searchResult.setSnippet(getHighlightedText(queryResponse, "content", (String) doc.get("id")));
                    break;
                case "linkToArchive":
                    searchResult.setLinkToArchive(waybackServiceEndpoint + "/" + oldestTimestamp + "/" + oldestUrl);
                    break;
                case "linkToNoFrame":
                    searchResult.setLinkToNoFrame(
                            waybackNoFrameServiceEndpoint + "/" + oldestTimestamp + "/" + oldestUrl);
                    break;
                case "linkToScreenshot":
                    try {
                        searchResult.setLinkToScreenshot(screenshotServiceEndpoint +
                                "?url="
                                + URLEncoder.encode(
                                        waybackNoFrameServiceEndpoint + "/" + oldestTimestamp + "/" + oldestUrl,
                                        StandardCharsets.UTF_8.toString()));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                case "linkToExtractedText":
                    try {
                        searchResult.setLinkToExtractedText(extractedTextServiceEndpoint.concat("?m=")
                                .concat(URLEncoder.encode(oldestUrl.concat("/").concat(oldestTimestamp),
                                        StandardCharsets.UTF_8.toString())));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                case "linkToMetadata":
                    try {
                        searchResult.setLinkToMetadata(textSearchServiceEndpoint.concat("?metadata=")
                                .concat(URLEncoder.encode(oldestUrl.concat("/").concat(oldestTimestamp),
                                        StandardCharsets.UTF_8.toString())));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                case "linkToOriginalFile":
                    searchResult.setLinkToOriginalFile(
                            waybackNoFrameServiceEndpoint + "/" + oldestTimestamp + "id_/" + oldestUrl);
                    break;

            }

        }
    }

    /**
     * Implementation of the SearchService query by URL (used in queryByUrl). This is only used for metadata and
     * textextracted to get a specific entry, so it will output at most one entry. Deduplication and snippet is 
     * turned off for this query. Site search does not use this, it's handled by the other query function.
     */
    @Override
    public SearchResults query(SearchQuery searchQuery, boolean urlSearch) {
        if (urlSearch) {
            String queryTerms = searchQuery.getQueryTerms();
            String tstamp = searchQuery.getFrom();
            List<String> solrQueryForSites = Arrays.asList(queryTerms.split(",")).stream()
                    .filter(url -> Utils.urlValidator(url))
                    .map(url -> URLNormalizers.canocalizeSurtUrl(url))
                    .map(surt -> "urlTimestamp:" + "*/" + Utils.canocalizeTimestamp(tstamp) + "/" + ClientUtils.escapeQueryChars(surt))
                    .collect(Collectors.toList());
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.set("q", String.join(" OR ", solrQueryForSites));
            solrQuery.set("fl","id,type,tstamp,urlTimestamp,surt,title,collection,url");
            solrQuery.set("hl","false");

            LOG.info("Solr Query (queryByUrl): "+solrQuery);
            searchQuery.setFields(new String[] { "title", "originalURL", "mimeType", "tstamp", "digest", "collection", "id",
            "linkToArchive", "linkToNoFrame", "linkToScreenshot", "linkToExtractedText",
            "linkToMetadata", "linkToOriginalFile" }); // Everything except the snippet
           
            try {
                QueryResponse queryResponse = this.getSolrClient().query(solrQuery);
                SearchResults searchResults = parseQueryResponse(queryResponse, searchQuery);
                return searchResults;
            } catch (SolrServerException | IOException e) {
                LOG.error("Error querying Solr: ", e);
                return null;
            }
        } else {    
            return query(searchQuery);
        }
    }

    /**
     * Implementation of the SearchService query. This is the function that is used for most queries.
     */
    @Override
    public SearchResults query(SearchQuery searchQuery) {
        SolrQuery solrQuery = convertSearchQuery(searchQuery);
        try {
            QueryResponse queryResponse = this.getSolrClient().query(solrQuery);
            SearchResults searchResults = parseQueryResponse(queryResponse, searchQuery);
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
