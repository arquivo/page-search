package pt.arquivo.services.solr;

import org.apache.solr.client.solrj.SolrQuery;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public HttpSolrClient getSolrClient() {
        if (this.solrClient == null) {
            LOG.info("Initing SolrClient pointing to " + this.baseSolrUrl);
            this.solrClient = new HttpSolrClient.Builder(this.baseSolrUrl).build();
        }
        return this.solrClient;
    }

    // TODO refactor this - extract duplicate code
    private void populateEndpointsLinks(SearchResultSolrImpl searchResult) throws UnsupportedEncodingException {
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

    private void addDeduplicationFilterQuery(SolrQuery solrQuery, String dedupField) {
        if (dedupField.equalsIgnoreCase("url")) {
            dedupField = "surt_url";
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{!collapse field=")
                .append(dedupField).append("}");
        solrQuery.addFilterQuery(stringBuilder.toString());
    }

    private SolrQuery convertSearchQuery(SearchQuery searchQuery) {
        SolrQuery solrQuery = new SolrQuery();

        solrQuery.setQuery(searchQuery.getQueryTerms());
        solrQuery.setStart(searchQuery.getOffset());
        solrQuery.setRows(searchQuery.getMaxItems());

        // enable highlighting
        solrQuery.setHighlight(true);

        if (searchQuery.isSearchByCollection()) {
            boolean multipleCollection = false;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("collection:");
            for (String collection : searchQuery.getCollection()) {
                if (multipleCollection) stringBuilder.append(" OR ");
                stringBuilder.append(collection);
                multipleCollection = true;
            }
            solrQuery.addFilterQuery(stringBuilder.toString());
        }

        if (searchQuery.isTimeBoundedQuery()) {
            String dateEnd = searchQuery.getTo() != null ? searchQuery.getTo() : "*";
            solrQuery.addFilterQuery("tstamp:[ " + searchQuery.getFrom() + " TO " + dateEnd + " ]");
        }

        if (searchQuery.isSearchBySite()) {
            String[] sites = searchQuery.getSite();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("site:");
            boolean multipleSite = false;
            for (String site : sites) {
                if (multipleSite) stringBuilder.append(" OR ");
                // strip out protocol and www's
                site = URLNormalizers.stripProtocolAndWWWUrl(site);
                stringBuilder.append("*.")
                        .append(site)
                        .append(" OR ")
                        .append("site:")
                        .append(site);
                multipleSite = true;
            }
            solrQuery.addFilterQuery(stringBuilder.toString());
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
            solrQuery.setFields(stringBuilderFields.toString());
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
            SearchResultSolrImpl searchResult = new SearchResultSolrImpl();
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
            searchResult.setId((String) doc.getFieldValue("id"));
            searchResult.setSolrClient(this.solrClient);
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
    public SearchResults query(SearchQuery searchQuery, boolean urlSearch) {
        if (urlSearch) {
            String queryTerms = searchQuery.getQueryTerms();
            // TODO validate if it is a URL ??
            // TODO transform in surt_url?
            searchQuery.setQueryTerms("url:\"".concat(queryTerms).concat("\""));
        }
        return query(searchQuery);
    }

    @Override
    public SearchResults query(SearchQuery searchQuery) {
        SolrQuery solrQuery = convertSearchQuery(searchQuery);
        try {
            QueryResponse queryResponse = this.getSolrClient().query(solrQuery);
            SearchResults searchResults = parseQueryResponse(queryResponse);
            return searchResults;
        } catch (SolrServerException e) {
            // TODO log this properly
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
