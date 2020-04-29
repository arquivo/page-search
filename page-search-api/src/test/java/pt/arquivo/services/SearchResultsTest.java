package pt.arquivo.services;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SearchResultsTest {

    @Test
    public void isLastPageResults() {
        SearchResults searchResults = new SearchResults();
        assertThat(searchResults.isLastPageResults()).isEqualTo(false);
    }
}