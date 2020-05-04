package pt.arquivo.services.nutchwax;

import org.archive.access.nutch.NutchwaxConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import pt.arquivo.services.SearchQueryImpl;
import pt.arquivo.services.SearchResult;
import pt.arquivo.services.SearchResultImpl;
import pt.arquivo.services.SearchResults;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
public class NutchWaxSearchServiceTestIT {

    @Configuration
    static class ContextConfiguration {
        @Bean
        public NutchWaxSearchService initNutchWaxService() throws IOException {
            org.apache.hadoop.conf.Configuration configuration = NutchwaxConfiguration.getConfiguration();
            String fullPath = getClass().getClassLoader().getResource("search-servers.txt").getPath();
            String basePath = fullPath.substring(0, fullPath.lastIndexOf("/"));
            configuration.set("searcher.dir", basePath);
            return new NutchWaxSearchService(configuration);
        }
    }


    @Autowired
    private NutchWaxSearchService nutchWaxSearchService;

    // @Test public void testUrlQuery(){
    //     SearchQueryImpl searchQuery = new SearchQueryImpl("versionHistory=expresso.pt&maxItems=5");
    //     searchQuery.setQ
    //     SearchResults searchResults = this.nutchWaxSearchService.query(searchQuery,true);
    //     // assertThat(searchResults.isLastPageResults()).isFalse();
    //     assertThat(searchResults.getEstimatedNumberResults()).isEqualTo(199);

    //     ArrayList<SearchResult> arraySearchResult = searchResults.getResults();
    //     SearchResultImpl firstSearchResult = (SearchResultImpl) arraySearchResult.get(0);

    //     assertThat(firstSearchResult.getStatusCode()).isEqualTo(200);
    //     // check only if the fields exist
    //     assertThat(firstSearchResult.getTitle()).isNotNull();
    //     assertThat(firstSearchResult.getOriginalURL()).isNotNull();
    //     assertThat(firstSearchResult.getLinkToArchive()).isNotNull();
    //     assertThat(firstSearchResult.getTstamp()).isNotNull();
    //     assertThat(firstSearchResult.getContentLength()).isNotNull();
    //     assertThat(firstSearchResult.getDigest()).isNotNull();
    //     assertThat(firstSearchResult.getLinkToScreenshot()).isNotNull();
    //     assertThat(firstSearchResult.getDate()).isNotNull();
    //     assertThat(firstSearchResult.getEncoding()).isBlank();
    //     assertThat(firstSearchResult.getLinkToNoFrame()).isNotNull();
    //     // TODO this will stop to be blank, waiting for cdx indexes update
    //     assertThat(firstSearchResult.getCollection()).isBlank();
    //     assertThat(firstSearchResult.getLinkToExtractedText()).isNotNull();
    //     assertThat(firstSearchResult.getLinkToMetadata()).isNotNull();
    //     assertThat(firstSearchResult.getLinkToOriginalFile()).isNotNull();
    // }

    @Test
    public void testMetadataRequest() {
        SearchQueryImpl searchQuery = new SearchQueryImpl("sapo");
        SearchResults searchResults = this.nutchWaxSearchService.query(searchQuery);
    }

    @Test
    public void testSimpleQueryFirstResult() throws UnsupportedEncodingException {
        SearchQueryImpl searchQuery = new SearchQueryImpl("sapo");
        SearchResults searchResults = this.nutchWaxSearchService.query(searchQuery);
        assertThat(searchResults.isLastPageResults()).isTrue();
        assertThat(searchResults.getEstimatedNumberResults()).isEqualTo(110);
        assertThat(searchResults.getNumberResults()).isEqualTo(41);

        ArrayList<SearchResult> arraySearchResult = searchResults.getResults();
        SearchResultImpl firstSearchResult = (SearchResultImpl) arraySearchResult.get(0);

        assertThat(firstSearchResult.getCollection()).isEqualTo("Roteiro");
        assertThat(firstSearchResult.getTitle()).isEqualTo("SAPO, Servidor de Apontadores Portugueses");
        assertThat(firstSearchResult.getOriginalURL()).isEqualTo("http://sapo.ua.pt/");
        assertThat(firstSearchResult.getDigest()).isEqualTo("6460448f99a916dc2b5d93b5bdacd169");
        assertThat(firstSearchResult.getMimeType()).isEqualTo("text/html");
        assertThat(firstSearchResult.getFileName()).isEqualTo("AWP-Roteiro-20090510220155-00000");
        assertThat(firstSearchResult.getContentLength()).isEqualTo(7386);
        assertThat(firstSearchResult.getOffset()).isEqualTo(707338);
        assertThat(firstSearchResult.getSnippet()).isEqualTo("<em>SAPO</em>, Servidor de Apontadores Portugueses Servidor de Apontadores Portugueses Op&ccedil;&otilde;es Novidades Novos Links no <em>SAPO</em> , Congressos , Exposi&ccedil;&otilde;es , Cursos de Forma&ccedil;&atilde;o , ... Ensino e Investiga&ccedil;&atilde;o<span class=\"ellipsis\"> ... </span>- http://<em>sapo</em>.ua.pt:80/<span class=\"ellipsis\"> ... </span>");
        // assertThat(firstSearchResult.getStatusCode()).isNull();
        assertThat(firstSearchResult.getDate()).isEqualTo("0845218958");
        assertThat(firstSearchResult.getLinkToArchive()).isEqualTo(nutchWaxSearchService.getWaybackServiceEndpoint().concat("/19961013150238/http://sapo.ua.pt/"));
        assertThat(firstSearchResult.getLinkToNoFrame()).isEqualTo(nutchWaxSearchService.getWaybackNoFrameServiceEndpoint().concat("/19961013150238/http://sapo.ua.pt/"));
        assertThat(firstSearchResult.getLinkToExtractedText())
                .isEqualTo(nutchWaxSearchService.getExtractedTextServiceEndpoint().concat("?m=19961013150238%2Fhttp%3A%2F%2Fsapo.ua.pt%2F"));
        assertThat(firstSearchResult.getLinkToMetadata()).isEqualTo(nutchWaxSearchService.getTextSearchServiceEndpoint()
                .concat("?metadata=19961013150238%2Fhttp%3A%2F%2Fsapo.ua.pt%2F"));
        String endpoint = nutchWaxSearchService.getScreenshotServiceEndpoint().concat("?url=").concat(URLEncoder.encode(nutchWaxSearchService.getWaybackNoFrameServiceEndpoint().concat("/19961013150238/http://sapo.ua.pt/"), StandardCharsets.UTF_8.toString()));
        assertThat(firstSearchResult.getLinkToScreenshot()).isEqualTo(endpoint);
        assertThat(firstSearchResult.getLinkToOriginalFile()).isEqualTo(nutchWaxSearchService.getWaybackNoFrameServiceEndpoint().concat("/19961013150238id_/http://sapo.ua.pt/"));
    }

    @Test
    public void testResultsBoundedQuery(){
        SearchQueryImpl searchQuery = new SearchQueryImpl("sapo");
        searchQuery.setOffset(5);
        searchQuery.setLimit(5);
        SearchResults searchResults = this.nutchWaxSearchService.query(searchQuery);

        assertThat(searchResults.isLastPageResults()).isFalse();
        assertThat(searchResults.getEstimatedNumberResults()).isEqualTo(110);
        // is querying extra results to know if is last page or not
        assertThat(searchResults.getNumberResults()).isEqualTo(11);
        assertThat(searchResults.getResults().size()).isEqualTo(5);
    }


    @Test
    public void testTimeBoundedQuery(){
        SearchQueryImpl searchQuery = new SearchQueryImpl("sapo");
        searchQuery.setFrom("19961013150238");
        searchQuery.setTo("19961013150305");

        SearchResults searchResults = this.nutchWaxSearchService.query(searchQuery);
        assertThat(searchResults.getResults().size()).isEqualTo(2);
    }

    @Test
    public void testCollectionBoundedQuery(){
        SearchQueryImpl searchQuery = new SearchQueryImpl("sapo");
        searchQuery.setCollection("outraqlq");

        SearchResults searchResults = this.nutchWaxSearchService.query(searchQuery);
        assertThat(searchResults.getResults().size()).isEqualTo(0);
    }

    @Test public void testSiteBoundedQuery(){
        SearchQueryImpl searchQuery = new SearchQueryImpl("sapo");
        searchQuery.setSite(new String[] {"http://sapo.ua.pt/"});

        SearchResults searchResults = this.nutchWaxSearchService.query(searchQuery);
        assertThat(searchResults.getResults().size()).isEqualTo(50);
    }

    /* What we want to test?
     * 1. simple text query - OK
     * 3. versionHistory - cdxService / PageSearchController
     * 4. metadata - PageSearchController
     * 5. timestamp validations - OK
     * 6. site search query - OK
     * 7. collection search query - OK
     * 8. last result page - OK
     * */
}