package pt.arquivo.services.fusion;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import pt.arquivo.services.SearchQuery;
import pt.arquivo.services.SearchQueryImpl;
import pt.arquivo.services.SearchResults;
import pt.arquivo.services.SearchResult;
import pt.arquivo.services.SearchService;
import pt.arquivo.services.SearchServiceConfiguration;
import pt.arquivo.services.nutchwax.ExternalNutchWaxSearchService;
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

    private ExternalNutchWaxSearchService nutchWaxSearchService = null;

    @Bean
    SolrSearchService generateSolrService() {
        return new SolrSearchService(this.getConfiguration());
    }

    @Bean
    ExternalNutchWaxSearchService generateExternalNutchWaxService() {
        return new ExternalNutchWaxSearchService();
    }

    private SolrSearchService getSolrSearchService(){
        if (solrSearchService == null){
            solrSearchService = generateSolrService();
        }
        return solrSearchService;
    }

    private ExternalNutchWaxSearchService getNutchWaxSearchService(){
        if(nutchWaxSearchService == null){
            nutchWaxSearchService = generateExternalNutchWaxService();
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

    // TODO: do proper merging
    @Override
    public SearchResults query(SearchQuery searchQuery, boolean urlSearch) {
        SearchResults solrResults = getSolrSearchService().query(searchQuery, urlSearch);
        SearchResults nutchResults = getNutchWaxSearchService().query(searchQuery, urlSearch);

        return mergeResults(solrResults, nutchResults);
    }

    // TODO: do proper merging
    @Override
    public SearchResults queryByTitle(SearchQuery searchQuery) {
        SearchResults solrResults = getSolrSearchService().queryByTitle(searchQuery);
        SearchResults nutchResults = getNutchWaxSearchService().queryByTitle(searchQuery);

        return mergeResults(solrResults, nutchResults);
    }

    @Override
    public SearchResults query(SearchQuery searchQuery) {
        int totalMaxItems = searchQuery.getMaxItems();
        int totalOffset = searchQuery.getOffset();
        int page = totalOffset/totalMaxItems;

        int odd = totalMaxItems % 2;

        // Half of the query will be made to Solr
        int solrMaxItems = totalMaxItems / 2 + odd;
        int solrOffset = page*solrMaxItems;
        SearchQuery solrSearchQuery = cloneSearchQuery(searchQuery);
        solrSearchQuery.setMaxItems(solrMaxItems);
        solrSearchQuery.setOffset(solrOffset);

        // The other half to the external API using nutchwax
        int nutchMaxItems = totalMaxItems / 2;
        int nutchOffset = page*nutchMaxItems;
        SearchQuery nutchSearchQuery = cloneSearchQuery(searchQuery);
        nutchSearchQuery.setMaxItems(nutchMaxItems);
        nutchSearchQuery.setOffset(nutchOffset);

        final SearchResults[] results = new SearchResults[2];

        // Using threads to allow both requests to be made in parallel
        Thread solrThread = new Thread(() -> {
            results[0] = getSolrSearchService().query(solrSearchQuery);
        });
        Thread nutchThread = new Thread(() -> {
            results[1] = getNutchWaxSearchService().query(nutchSearchQuery);
        });

        solrThread.start();
        nutchThread.start();

        try {
            solrThread.join();
        } catch (InterruptedException e) {
            LOG.error("FusionSearchService - Something went wrong with the Solr request: ", e);
        }

        try {
            nutchThread.join();
        } catch (InterruptedException e) {
            LOG.error("FusionSearchService - Something went wrong with the Nutch request: ", e);
        }
        
        SearchResults solrResults = results[0];
        SearchResults nutchResults = results[1];

        return mergeResults(solrResults, nutchResults);
    }

    private SearchQuery cloneSearchQuery(SearchQuery searchQuery){
        SearchQuery r = new SearchQueryImpl(searchQuery.getQueryTerms());
        if (searchQuery.getOffset() > 0) r.setOffset(searchQuery.getOffset());
        if (searchQuery.getMaxItems() != 50) r.setMaxItems(searchQuery.getMaxItems());
        if (searchQuery.getFrom() != null) r.setFrom(searchQuery.getFrom());
        if (searchQuery.getTo() != null) r.setTo(searchQuery.getTo());
        if (searchQuery.getType() != null) r.setType(searchQuery.getType());
        if (searchQuery.getSite() != null) r.setSite(searchQuery.getSite());
        if (searchQuery.getCollection() != null) r.setCollection(searchQuery.getCollection());
        if (searchQuery.getDedupField() != null) r.setDedupField(searchQuery.getDedupField());
        if (searchQuery.getDedupValue() != 2) r.setDedupValue(searchQuery.getDedupValue());
        return r;
    }
}
