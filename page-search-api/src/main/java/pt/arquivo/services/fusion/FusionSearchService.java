package pt.arquivo.services.fusion;

import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import pt.arquivo.services.SearchQuery;
import pt.arquivo.services.SearchResults;
import pt.arquivo.services.SearchResult;
import pt.arquivo.services.SearchService;
import pt.arquivo.services.SearchServiceConfiguration;
import pt.arquivo.services.nutchwax.NutchWaxSearchService;
import pt.arquivo.services.solr.SolrSearchService;

public class FusionSearchService implements SearchService {

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

    @Value("${searchpages.api.show.ids:false}")
    private boolean showIds;

    private static final Logger LOG = LoggerFactory.getLogger(SolrSearchService.class);

    private static SearchServiceConfiguration configuration = null;

    private SearchServiceConfiguration getConfiguration(){
        if(configuration == null){
            configuration = new SearchServiceConfiguration();
            configuration.setStartDate(startDate);
            configuration.setServiceName(serviceName);
            configuration.setScreenshotServiceEndpoint(screenshotServiceEndpoint);
            configuration.setWaybackServiceEndpoint(waybackServiceEndpoint);
            configuration.setWaybackNoFrameServiceEndpoint(waybackNoFrameServiceEndpoint);
            configuration.setExtractedTextServiceEndpoint(extractedTextServiceEndpoint);
            configuration.setBaseSolrUrl(baseSolrUrl);
            configuration.setTextSearchServiceEndpoint(textSearchServiceEndpoint);
            configuration.setShowIds(showIds);
        }
        return configuration;
    }

    private SolrSearchService solrSearchService = null;

    private NutchWaxSearchService nutchWaxSearchService = null;

    private SolrSearchService getSolrSearchService(){
        if (solrSearchService == null){
            solrSearchService = new SolrSearchService(this.getConfiguration());
        }
        return solrSearchService;
    }

    private NutchWaxSearchService getNutchWaxSearchService(){
        if(nutchWaxSearchService == null){
            try {
                nutchWaxSearchService = new NutchWaxSearchService();
            } catch (IOException e) {
                LOG.error("[FusionSearchService] - Failed to load NutchWax search service: " + e);
                return null;
            }
        }
        return nutchWaxSearchService;
    }

    private SearchResults mergeResults(SearchResults solrResults, SearchResults nutchResults) {
        SearchResults mergedResults = new SearchResults();
        ArrayList<SearchResult> mergedResultArray = new ArrayList<SearchResult>();
        mergedResultArray.addAll(solrResults.getResults());
        mergedResultArray.addAll(nutchResults.getResults());
        mergedResults.setResults(mergedResultArray);
        mergedResults.setEstimatedNumberResults(
                solrResults.getEstimatedNumberResults() + nutchResults.getEstimatedNumberResults());
        mergedResults.setNumberResults(solrResults.getNumberResults() + nutchResults.getNumberResults());
        mergedResults.setLastPageResults(solrResults.isLastPageResults() && nutchResults.isLastPageResults());
        return mergedResults;
    }

    @Override
    public SearchResults query(SearchQuery searchQuery, boolean urlSearch) {
        SearchResults solrResults = getSolrSearchService().query(searchQuery, urlSearch);
        SearchResults nutchResults = getNutchWaxSearchService().query(searchQuery, urlSearch);

        return mergeResults(solrResults, nutchResults);
    }

    @Override
    public SearchResults query(SearchQuery searchQuery) {
        SearchResults solrResults = getSolrSearchService().query(searchQuery);
        SearchResults nutchResults = getNutchWaxSearchService().query(searchQuery);

        return mergeResults(solrResults, nutchResults);
    }
}
