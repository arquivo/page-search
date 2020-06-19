package pt.arquivo.services.solr;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.arquivo.indexer.utils.URLNormalizers;
import pt.arquivo.services.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SolrSearchService implements SearchService {

    private static final Logger LOG = LoggerFactory.getLogger(SolrSearchService.class);
    private static final Pattern stripWWWNRuleREGEX = Pattern.compile("^(?:https?://)(?:www[0-9]*\\.)?([^/]*/?.*)$");

    // TODO should upgrade this for the SolrCloudClient
    private HttpSolrClient solrClient;

    @Value("${searchpages.api.startdate}")
    private String startDate;

    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    @Value("${searchpages.service.link}")
    private String serviceName;

    @Value("${screenshot.service.endpoint}")
    private String screenshotServiceEndpoint;

    @Value("${wayback.service.endpoint}")
    private String waybackServiceEndpoint;

    @Value("${wayback.noframe.service.endpoint}")
    private String waybackNoFrameServiceEndpoint;

    @Value("${searchpages.extractedtext.service.link}")
    private String extractedTextServiceEndpoint;

    @Value("${searchpages.solr.service.link}")
    private String baseSolrUrl;

    @Value("${searchpages.textsearch.service.link}")
    private String textSearchServiceEndpoint;

    // TODO refactor this - extract duplicate code
    private void populateEndpointsLinks(SearchResultImpl searchResult) throws UnsupportedEncodingException {
        searchResult.setLinkToArchive(waybackServiceEndpoint +
                "/" + searchResult.getTstamp() +
                "/" + searchResult.getOriginalURL());

        searchResult.setLinkToNoFrame(waybackNoFrameServiceEndpoint +
                "/" + searchResult.getTstamp() +
                "/" + searchResult.getOriginalURL());

        searchResult.setLinkToScreenshot(screenshotServiceEndpoint +
                "?url=" + URLEncoder.encode(searchResult.getLinkToNoFrame(), StandardCharsets.UTF_8.toString()));

        searchResult.setLinkToExtractedText(extractedTextServiceEndpoint.concat("?m=")
                .concat(URLEncoder.encode(searchResult.getOriginalURL().concat("/").concat(searchResult.getTstamp()), StandardCharsets.UTF_8.toString())));

        searchResult.setLinkToMetadata(textSearchServiceEndpoint.concat("?metadata=")
                .concat(URLEncoder.encode(searchResult.getOriginalURL().concat("/").concat(searchResult.getTstamp()), StandardCharsets.UTF_8.toString())));

        searchResult.setLinkToOriginalFile(waybackNoFrameServiceEndpoint +
                "/" + searchResult.getTstamp() +
                "id_/" + searchResult.getOriginalURL());
    }

    private SolrParams convertSearchQuery(SearchQuery searchQuery) {
        Map<String, String> queryParamMap = new HashMap<String, String>();
        queryParamMap.put("q", searchQuery.getQueryTerms());
        queryParamMap.put("start", String.valueOf(searchQuery.getOffset()));
        queryParamMap.put("rows", String.valueOf(searchQuery.getMaxItems()));

        // enable highlighting
        queryParamMap.put("hl", "on");

        if (searchQuery.getCollection() != null) {
            for(String collection : searchQuery.getCollection()){
                queryParamMap.put("fq", "collection:" + collection);
            }
        }

        if (searchQuery.isTimeBoundedQuery()) {
            String dateEnd = searchQuery.getTo() != null ? searchQuery.getTo() : "*";
            queryParamMap.put("fq", "tstamp:[ " + searchQuery.getFrom() + " TO " + dateEnd + " ]");
        }

        if (searchQuery.isSearchBySite()){
            String[] sites = searchQuery.getSite();
            for(String site : sites){
                // strip out protocol and www's
                site = URLNormalizers.stripProtocolAndWWWUrl(site);
                queryParamMap.put("fq", "site:*." + site);
                queryParamMap.put("fq", "site:" + site);
            }
        }

        // filter fields
        if (searchQuery.getFields() != null) {
            String[] fieldsArray = searchQuery.getFields();
            StringBuilder stringBuilderFields = new StringBuilder();
            for (int i = 0; i <= fieldsArray.length - 1; i++) {
                stringBuilderFields.append(fieldsArray[i]);
                stringBuilderFields.append(",");
            }
            stringBuilderFields.append(fieldsArray[fieldsArray.length - 1]);

            queryParamMap.put("fl", stringBuilderFields.toString());
        }

        MapSolrParams queryParams = new MapSolrParams(queryParamMap);
        return queryParams;
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

    // TODO REVIEW THIS
    private static final String getFragments(List<String> snippets) {
        StringBuilder fragments = new StringBuilder();
        for (int i = 0; i < snippets.size(); i++) {
            if (i > 0) {
                fragments.append("............");
            }
            fragments.append(snippets.get(i));
        }
        return fragments.toString();
    }


    private SearchResults parseQueryResponse(QueryResponse queryResponse) {
        SearchResults searchResults = new SearchResults();
        ArrayList<SearchResult> searchResultArrayList = new ArrayList<>();

        SolrDocumentList solrDocumentList = queryResponse.getResults();
        for (SolrDocument doc : solrDocumentList) {

            // TODO change this to SolrSearchResult or generalize
            SearchResultImpl searchResult = new SearchResultImpl();
            searchResult.setTitle((String) doc.getFieldValue("title"));
            searchResult.setOriginalURL((String) doc.getFieldValue("url"));
            searchResult.setMimeType((String) doc.getFieldValue("type"));
            searchResult.setTstamp((Long) doc.getFieldValue("tstamp"));
            searchResult.setOffset((Long) doc.getFieldValue("warc_offset"));
            searchResult.setFileName((String) doc.getFieldValue("warc_name"));
            searchResult.setCollection((String) doc.getFieldValue("collection"));
            searchResult.setContentLength((Long) doc.getFieldValue("content_length"));
            searchResult.setDigest((String) doc.getFieldValue("digest"));
            searchResult.setEncoding((String) doc.getFieldValue("encoding"));
            // TODO add missing fields

            searchResult.setSnippet(getHighlightedText(queryResponse, "content", (String) doc.get("id")));
            try {
                populateEndpointsLinks(searchResult);
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
    public SearchResults query(SearchQuery searchQuery, boolean urlSearch){
        return query(searchQuery);
    }

    @Override
    public SearchResults query(SearchQuery searchQuery) {

        // TODO have this already instanced before doing the query. Is not that way because how @Value works
        LOG.info("Initing SolrClient pointing to " + this.baseSolrUrl);
        this.solrClient = new HttpSolrClient.Builder(this.baseSolrUrl).build();

        if (searchQuery.isSearchBySite() && searchQuery.getDedupField() == null) {
            searchQuery.setDedupField("url");
        } else {
            searchQuery.setDedupField("site");
        }

        SolrParams solrParams = convertSearchQuery(searchQuery);
        try {
            QueryResponse queryResponse = this.solrClient.query(solrParams);
            SearchResults searchResults = parseQueryResponse(queryResponse);
            return searchResults;
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SearchResults searchResults = new SearchResults();
        searchResults.setEstimatedNumberResults(0);
        searchResults.setNumberResults(0);
        return searchResults;
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
