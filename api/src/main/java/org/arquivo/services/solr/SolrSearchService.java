package org.arquivo.services.solr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.arquivo.services.SearchQuery;
import org.arquivo.services.SearchResult;
import org.arquivo.services.SearchResults;
import org.arquivo.services.SearchService;
import org.arquivo.services.SearchResultImpl;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SolrSearchService implements SearchService {

    private static final Log LOG = LogFactory.getLog(SolrSearchService.class);
    // TODO should upgrade this for the SolrCloudClient
    private HttpSolrClient solrClient;

    @Value("${searchpages.solr.service.link}")
    private String baseSolrUrl;

    public SolrSearchService(){
    }

    private boolean isTimeBoundedQuery(SearchQuery searchQuery) {
        return (searchQuery.getTo() != null || searchQuery.getFrom() != null);
    }

    SolrParams convertSearchQuery(SearchQuery searchQuery){
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

        if (searchQuery.getCollection() != null){
            queryParamMap.put("fq", "collection:" + searchQuery.getCollection());
        }

        if (isTimeBoundedQuery(searchQuery)) {
            String dateEnd = searchQuery.getTo() != null ? searchQuery.getTo() : "*";
            queryParamMap.put("fq", "timestamp:[" + searchQuery.getFrom() + "TO" + dateEnd + "]" );
        }

        // filter fields
        if (searchQuery.getFields() != null){
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

    SearchResults parseQueryResponse(QueryResponse queryResponse){
        SearchResults searchResults = new SearchResults();
        ArrayList<SearchResult> searchResultArrayList = new ArrayList<>();

        SolrDocumentList solrDocumentList = queryResponse.getResults();
        for (SolrDocument doc : solrDocumentList){
            // TODO change this to SolrSearchResult or generalize
            SearchResultImpl searchResult = new SearchResultImpl();
            searchResult.setOriginalURL((String) doc.getFieldValue("url"));
            searchResult.setMimeType((String) doc.getFieldValue("type"));
            searchResult.setTimeStamp((Long) doc.getFieldValue("tstamp"));

            searchResultArrayList.add(searchResult);
        }
        searchResults.setResults(searchResultArrayList);
        searchResults.setNumberEstimatedResults(queryResponse.getResults().getNumFound());
        searchResults.setNumberResults(queryResponse.getResults().size());

        return searchResults;
    }

    // TODO Which SearchQuery to use? apparently we can still use the same as NutchWaxSearchQuery
    @Override
    public SearchResults query(SearchQuery searchQuery) {
        // TODO have this already instanciated before doing the query. Is not that way because how @Value works
        LOG.info("Initing SolrClient poiting to "+ this.baseSolrUrl);
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
        searchResults.setNumberEstimatedResults(0);
        searchResults.setNumberResults(0);
        return searchResults;
    }
}
