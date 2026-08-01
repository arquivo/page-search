package pt.arquivo.services.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Test;
import pt.arquivo.services.SearchQuery;
import pt.arquivo.services.SearchQueryImpl;
import static org.assertj.core.api.Assertions.assertThat;

public class SolrSearchServiceTest {

    private final SolrSearchService solrSearchService = new SolrSearchService();

    @Test
    public void isLastPage() {
        SearchQuery searchQuery = new SearchQueryImpl("sapo");
        searchQuery.setOffset(10);
        searchQuery.setMaxItems(10);
        assertThat(SolrSearchService.isLastPage(20, searchQuery)).isTrue();
        assertThat(SolrSearchService.isLastPage(21, searchQuery)).isFalse();
    }

    @Test
    public void searchByLanguage() {
        SearchQuery searchQuery = new SearchQueryImpl("sapo");
        searchQuery.setLanguage("pt");

        // filtering by language only takes the confident detections by default
        SolrQuery solrQuery = solrSearchService.convertSearchQuery(searchQuery);
        assertThat(solrQuery.getFilterQueries()).contains("language:pt", "languageConfidence:(HIGH)");
    }

    @Test
    public void searchByMinLanguageConfidence() {
        SearchQuery searchQuery = new SearchQueryImpl("sapo");
        searchQuery.setLanguage("pt");
        searchQuery.setMinLanguageConfidence("MEDIUM");

        // MEDIUM is a threshold, it takes the HIGH confidence documents too
        SolrQuery solrQuery = solrSearchService.convertSearchQuery(searchQuery);
        assertThat(solrQuery.getFilterQueries()).contains("language:pt", "languageConfidence:(HIGH OR MEDIUM)");

        // the least confident tier is indexed as NONE
        searchQuery.setMinLanguageConfidence("LOW");
        solrQuery = solrSearchService.convertSearchQuery(searchQuery);
        assertThat(solrQuery.getFilterQueries()).contains("languageConfidence:(HIGH OR MEDIUM OR NONE)");
    }

    @Test
    public void searchWithoutLanguage() {
        SearchQuery searchQuery = new SearchQueryImpl("sapo");

        // a query that doesn't ask about the language isn't filtered by language confidence
        SolrQuery solrQuery = solrSearchService.convertSearchQuery(searchQuery);
        assertThat(solrQuery.getFilterQueries()).noneMatch(fq -> fq.startsWith("language"));
    }

    @Test
    public void languageResultFields() {
        SearchQuery searchQuery = new SearchQueryImpl("sapo");
        assertThat(solrSearchService.convertSearchQuery(searchQuery).getFields()).doesNotContain("language");

        searchQuery.setFields(new String[]{"title", "language"});
        assertThat(solrSearchService.convertSearchQuery(searchQuery).getFields())
                .contains("language")
                .doesNotContain("languageConfidence");

        searchQuery.setFields(new String[]{"title", "language", "languageConfidence"});
        assertThat(solrSearchService.convertSearchQuery(searchQuery).getFields()).contains("languageConfidence");
    }
}