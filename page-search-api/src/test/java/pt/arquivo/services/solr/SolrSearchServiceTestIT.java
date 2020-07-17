package pt.arquivo.services.solr;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import pt.arquivo.services.SearchQueryImpl;
import pt.arquivo.services.SearchResult;
import pt.arquivo.services.SearchResultImpl;
import pt.arquivo.services.SearchResults;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class SolrSearchServiceTestIT {

    @Configuration
    @TestPropertySource(properties = { "searchpages.textsearch.service.bean=solr"})
    @PropertySource("classpath:application.properties")
    static class ContextConfiguration {
        @Bean
        static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        public SolrSearchService initSolrService() {
            return new SolrSearchService();
        }
    }

    @Autowired
    private SolrSearchService solrSearchService;

    @Test
    public void testMetadataRequest() {
        SearchQueryImpl searchQuery = new SearchQueryImpl("sapo");
        SearchResults searchResults = this.solrSearchService.query(searchQuery);
        // TODO Verify number of results with NutchWaxBackend
        // assertThat(searchResults.getNumberResults()).isEqualTo(41);
        // assertThat(searchResults.isLastPageResults()).isTrue();

        // verify first result
        // SearchResultImpl searchResult = (SearchResultImpl) searchResults.getResults().get(0);
        // assertThat(searchResult.getTitle()).isEqualTo("SAPO, Servidor de Apontadores Portugueses");
        // assertThat(searchResult.getCollection()).isEqualTo("Roteiro");
        // assertThat(searchResult.getStatusCode()).isNull();
        // assertThat(searchResult.getTstamp()).isEqualTo("19961013150238");
        // assertThat(searchResult.getId()).isNull();
        // assertThat(searchResult.getDigest()).isEqualTo("6460448f99a916dc2b5d93b5bdacd169");
    }


    @Test
    public void testSimpleQueryFirstResult() throws UnsupportedEncodingException {
        SearchQueryImpl searchQuery = new SearchQueryImpl("sapo");
        SearchResults searchResults = this.solrSearchService.query(searchQuery);
        // assertThat(searchResults.isLastPageResults()).isTrue();
        assertThat(searchResults.getEstimatedNumberResults()).isEqualTo(36);
        assertThat(searchResults.getNumberResults()).isEqualTo(36);

        ArrayList<SearchResult> arraySearchResult = searchResults.getResults();
        SearchResultImpl firstSearchResult = (SearchResultImpl) arraySearchResult.get(0);

        // TODO Teste this later when the result order is not always changing.
        // assertThat(firstSearchResult.getCollection()).isEqualTo("Roteiro");
        /*
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
        assertThat(firstSearchResult.getLinkToArchive()).isEqualTo(this.solrSearchService.getWaybackServiceEndpoint().concat("/19961013150238/http://sapo.ua.pt/"));
        assertThat(firstSearchResult.getLinkToNoFrame()).isEqualTo(this.solrSearchService.getWaybackNoFrameServiceEndpoint().concat("/19961013150238/http://sapo.ua.pt/"));
        assertThat(firstSearchResult.getLinkToExtractedText())
                .isEqualTo(this.solrSearchService.getExtractedTextServiceEndpoint().concat("?m=http%3A%2F%2Fsapo.ua.pt%2F%2F19961013150238"));
        assertThat(firstSearchResult.getLinkToMetadata()).isEqualTo(this.solrSearchService.getTextSearchServiceEndpoint()
                .concat("?metadata=http%3A%2F%2Fsapo.ua.pt%2F%2F19961013150238"));
        String endpoint = this.solrSearchService.getScreenshotServiceEndpoint().concat("?url=").concat(URLEncoder.encode(this.solrSearchService.getWaybackNoFrameServiceEndpoint().concat("/19961013150238/http://sapo.ua.pt/"), StandardCharsets.UTF_8.toString()));
        assertThat(firstSearchResult.getLinkToScreenshot()).isEqualTo(endpoint);
        assertThat(firstSearchResult.getLinkToOriginalFile()).isEqualTo(this.solrSearchService.getWaybackNoFrameServiceEndpoint().concat("/19961013150238id_/http://sapo.ua.pt/"));
         */
    }

    @Test
    public void testResultsBoundedQuery() {
        SearchQueryImpl searchQuery = new SearchQueryImpl("sapo");
        searchQuery.setOffset(5);
        searchQuery.setMaxItems(5);
        SearchResults searchResults = this.solrSearchService.query(searchQuery);

        assertThat(searchResults.isLastPageResults()).isFalse();
        assertThat(searchResults.getEstimatedNumberResults()).isEqualTo(36);
        assertThat(searchResults.getNumberResults()).isEqualTo(5);
    }


    @Test
    public void testTimeBoundedQuery() {
        SearchQueryImpl searchQuery = new SearchQueryImpl("sapo");
        searchQuery.setFrom("19961013150238");
        searchQuery.setTo("19961013150305");

        SearchResults searchResults = this.solrSearchService.query(searchQuery);
        assertThat(searchResults.getResults().size()).isEqualTo(2);
    }

    @Test
    public void testCollectionBoundedQuery() {
        SearchQueryImpl searchQuery = new SearchQueryImpl("sapo");
        searchQuery.setCollection(new String[]{"collectionX"});

        SearchResults searchResults = this.solrSearchService.query(searchQuery);
        assertThat(searchResults.getResults().size()).isEqualTo(0);
    }

    @Test
    public void testSiteBoundedQuery() {
        SearchQueryImpl searchQuery = new SearchQueryImpl("sapo");
        searchQuery.setSite(new String[]{"http://sapo.ua.pt/"});
        searchQuery.setDedupField("url");
        searchQuery.setDedupValue(1);

        SearchResults searchResults = this.solrSearchService.query(searchQuery);
        assertThat(searchResults.getResults().size()).isEqualTo(50);
    }
}
