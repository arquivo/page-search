package pt.arquivo.services.fusion;

import java.io.IOException;
import java.util.ArrayList;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.arquivo.services.SearchQuery;
import pt.arquivo.services.SearchResults;
import pt.arquivo.services.SearchResult;
import pt.arquivo.services.SearchService;
import pt.arquivo.services.nutchwax.NutchWaxSearchService;
import pt.arquivo.services.solr.SolrSearchService;

public class FusionSearchService implements SearchService {

    private static final Logger LOG = LoggerFactory.getLogger(SolrSearchService.class);

    private SolrSearchService solrSearchService = null;
    private NutchWaxSearchService nutchWaxSearchService = null;

    private SolrSearchService getSolrSearchService() {
        if (solrSearchService == null){
            solrSearchService = new SolrSearchService();
        }
        return solrSearchService;
    }

    private NutchWaxSearchService getNutchWaxSearchService() {
        if (nutchWaxSearchService == null){
            try {
                nutchWaxSearchService = new NutchWaxSearchService();
            } catch (IOException e) {
                LOG.error("[FusionSearchService] - Failed to load NutchWax search service: " + e);
            }
        }
        return nutchWaxSearchService;
    }

    private SearchResults mergeResults(SearchResults solrResults, SearchResults nutchResults){
        SearchResults mergedResults = new SearchResults(); 
        ArrayList <SearchResult> mergedResultArray = new ArrayList <SearchResult>();
        mergedResultArray.addAll(solrResults.getResults());
        mergedResultArray.addAll(nutchResults.getResults());
        mergedResults.setResults(mergedResultArray);
        mergedResults.setEstimatedNumberResults(solrResults.getEstimatedNumberResults() + nutchResults.getEstimatedNumberResults());
        mergedResults.setNumberResults(solrResults.getNumberResults() + nutchResults.getNumberResults());
        mergedResults.setLastPageResults(solrResults.isLastPageResults() && nutchResults.isLastPageResults());
        return mergedResults;
    }

    @Override
    public SearchResults query(SearchQuery searchQuery, boolean urlSearch) {
        SearchResults solrResults = getSolrSearchService().query(searchQuery,urlSearch);
        SearchResults nutchResults = getNutchWaxSearchService().query(searchQuery,urlSearch);

        return mergeResults(solrResults, nutchResults);
    }
    
    @Override
    public SearchResults query(SearchQuery searchQuery) {
        SearchResults solrResults = getSolrSearchService().query(searchQuery);
        SearchResults nutchResults = getNutchWaxSearchService().query(searchQuery);

        return mergeResults(solrResults, nutchResults);
    }
}
