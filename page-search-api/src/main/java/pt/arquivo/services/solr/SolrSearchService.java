package pt.arquivo.services.solr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import pt.arquivo.services.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SolrSearchService implements SearchService {

    private static final Log LOG = LogFactory.getLog(SolrSearchService.class);
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

    public SolrSearchService() {
    }

    private boolean isTimeBoundedQuery(SearchQuery searchQuery) {
        return (searchQuery.getTo() != null || searchQuery.getFrom() != null);
    }

    private void populateEndpointsLinks(SearchResultImpl searchResult) {
        searchResult.setLinkToArchive(waybackServiceEndpoint +
                "/" + searchResult.getTstamp() +
                "/" + searchResult.getOriginalURL());

        searchResult.setLinkToScreenshot(screenshotServiceEndpoint +
                "?url=" + searchResult.getLinkToArchive());

        searchResult.setLinkToNoFrame(waybackNoFrameServiceEndpoint +
                "/" + searchResult.getTstamp() +
                "/" + searchResult.getOriginalURL());

        searchResult.setLinkToExtractedText(extractedTextServiceEndpoint +
                "?m=" + searchResult.getTstamp() +
                "/" + searchResult.getOriginalURL());
    }

    SolrParams convertSearchQuery(SearchQuery searchQuery) {
        // TODO add site search option, and make sure to canolize the URL
        /*
            getQueryTerms - "q"
            getOffset - "start"
            getLimit - "rows"
            getlimitPerSite
            getSite
            getType
            getCollection
            getFrom
            getTo
            getFields - "fl"
         */
        Map<String, String> queryParamMap = new HashMap<String, String>();
        queryParamMap.put("q", searchQuery.getQueryTerms());
        queryParamMap.put("start", String.valueOf(searchQuery.getOffset()));
        queryParamMap.put("rows", String.valueOf(searchQuery.getLimit()));

        // enable highlighting
        queryParamMap.put("hl", "on");

        if (searchQuery.getCollection() != null) {
            queryParamMap.put("fq", "collection:" + searchQuery.getCollection());
        }

        if (isTimeBoundedQuery(searchQuery)) {
            String dateEnd = searchQuery.getTo() != null ? searchQuery.getTo() : "*";
            queryParamMap.put("fq", "timestamp:[" + searchQuery.getFrom() + "TO" + dateEnd + "]");
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

        // TODO implement site Search
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


    SearchResults parseQueryResponse(QueryResponse queryResponse) {
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
            searchResult.setContentLength((Long) doc.getFieldValue("contentLength"));
            searchResult.setDigest((String) doc.getFieldValue("digest"));
            searchResult.setEncoding((String) doc.getFieldValue("encoding"));

            searchResult.setSnippet(getHighlightedText(queryResponse, "content", (String) doc.get("id")));
            populateEndpointsLinks(searchResult);
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

    // TODO Which SearchQuery to use? apparently we can still use the same as NutchWaxSearchQuery
    @Override
    public SearchResults query(SearchQuery searchQuery) {

        // TODO have this already instanciated before doing the query. Is not that way because how @Value works
        LOG.info("Initing SolrClient poiting to " + this.baseSolrUrl);
        this.solrClient = new HttpSolrClient.Builder(this.baseSolrUrl).build();

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
}
