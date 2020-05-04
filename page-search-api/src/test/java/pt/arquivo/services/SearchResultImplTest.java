package pt.arquivo.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.nutch.searcher.HitDetails;
import org.archive.access.nutch.NutchwaxBean;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

// TODO review this
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

    @Test
    public void testCustomSerialization() throws JsonProcessingException {
        SearchResultImpl searchResult = new SearchResultImpl();
        searchResult.setStatusCode(0);
        searchResult.setTitle("teste");

        HashMap<String, Object> hashMap = toJson(searchResult);

        // check if the fields are OK (without nulls and not wanted fields)
        // gson is casting to double
        assertThat(hashMap.get("contentLength")).isEqualTo(0.0);
        assertThat(hashMap.get("statusCode")).isNull();

        String[] fields = {"title"};
        searchResult.setFields(fields);
        hashMap = toJson(searchResult);

        assertThat((hashMap.get("title"))).isNotNull();
        assertThat((hashMap.get("contentLength"))).isNull();
        assertThat(hashMap.get("statusCode")).isNull();
    }

    private HashMap<String, Object> toJson(SearchResultImpl searchResult) throws JsonProcessingException {
        String json = new ObjectMapper().writeValueAsString(searchResult);
        Gson gson = new Gson();
        HashMap<String, Object> hashMap = gson.fromJson(json, HashMap.class);
        return hashMap;
    }
}