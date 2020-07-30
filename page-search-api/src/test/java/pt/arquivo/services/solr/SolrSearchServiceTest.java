package pt.arquivo.services.solr;

import org.junit.Test;
import pt.arquivo.services.SearchQuery;
import pt.arquivo.services.SearchQueryImpl;
import static org.assertj.core.api.Assertions.assertThat;

public class SolrSearchServiceTest {

    @Test
    public void isLastPage() {
        SearchQuery searchQuery = new SearchQueryImpl("sapo");
        searchQuery.setOffset(10);
        searchQuery.setMaxItems(10);
        assertThat(SolrSearchService.isLastPage(20, searchQuery)).isTrue();
        assertThat(SolrSearchService.isLastPage(21, searchQuery)).isFalse();
    }
}