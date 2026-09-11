package pt.arquivo.services.nutchwax;

import org.junit.Test;
import pt.arquivo.services.SearchQuery;
import pt.arquivo.services.SearchQueryImpl;

import static org.assertj.core.api.Assertions.assertThat;

public class NutchWaxSearchServiceTest {

    @Test
    public void isLastPage() {
        SearchQuery searchQuery = new SearchQueryImpl("sapo");
        searchQuery.setOffset(10);
        searchQuery.setMaxItems(10);
        assertThat(NutchWaxSearchService.isLastPage(21, searchQuery)).isFalse();
        assertThat(NutchWaxSearchService.isLastPage(15, searchQuery)).isTrue();
        assertThat(NutchWaxSearchService.isLastPage(20, searchQuery)).isTrue();
    }

}