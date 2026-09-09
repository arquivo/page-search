package pt.arquivo.services;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SearchResultSolrImplTest {

    @Test
    public void getSearchResultId_combinesTstampAndOriginalUrl() {
        SearchResultSolrImpl result = new SearchResultSolrImpl();
        result.setTimeStamp("20190101010101");
        result.setOriginalURL("http://example.com");

        assertThat(result.getSearchResultId()).isEqualTo("20190101010101/http://example.com");
    }

    @Test
    public void getExtractedText_queryToleratesUnavailableShards() throws Exception {
        SolrDocument doc = new SolrDocument();
        doc.addField("title", "title");
        doc.addField("content", "content");
        SolrDocumentList docList = new SolrDocumentList();
        docList.add(doc);

        QueryResponse queryResponse = mock(QueryResponse.class);
        when(queryResponse.getResults()).thenReturn(docList);

        SolrClient solrClient = mock(SolrClient.class);
        when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);

        SearchResultSolrImpl result = new SearchResultSolrImpl();
        result.setId("doc-1");
        result.setSolrClient(solrClient);

        result.getExtractedText();

        ArgumentCaptor<SolrQuery> solrQueryCaptor = ArgumentCaptor.forClass(SolrQuery.class);
        verify(solrClient).query(solrQueryCaptor.capture());
        assertThat(solrQueryCaptor.getValue().get("shards.tolerant")).isEqualTo("true");
    }
}
