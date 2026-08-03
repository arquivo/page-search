package pt.arquivo.services;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SearchResultSolrImplTest {

    @Test
    public void getSearchResultId_combinesTstampAndOriginalUrl() {
        SearchResultSolrImpl result = new SearchResultSolrImpl();
        result.setTimeStamp("20190101010101");
        result.setOriginalURL("http://example.com");

        assertThat(result.getSearchResultId()).isEqualTo("20190101010101/http://example.com");
    }
}
