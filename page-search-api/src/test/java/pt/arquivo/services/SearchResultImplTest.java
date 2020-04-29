package pt.arquivo.services;

import org.apache.nutch.searcher.HitDetails;
import org.archive.access.nutch.NutchwaxBean;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SearchResultImplTest {

    @Test
    public void getExtractedText() throws IOException {
        NutchwaxBean bean = mock(NutchwaxBean.class);
        Mockito.when(bean.getParseText((HitDetails) Mockito.any())).thenThrow(new IOException());

        SearchResultImpl searchResult = new SearchResultImpl();
        searchResult.setBean(bean);

        String extractedText = searchResult.getExtractedText();
        assertThat(extractedText.equalsIgnoreCase(""));
    }
}