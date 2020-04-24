package pt.arquivo.api;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import pt.arquivo.services.SearchResult;
import pt.arquivo.services.SearchResultImpl;
import pt.arquivo.services.SearchResults;
import pt.arquivo.services.SearchService;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = PageSearchApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class PageSearchControllerTest {

    @MockBean
    private SearchService searchService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void searchUrl() {
    }

    @Test
    public void extractedText() {
    }

    @Test
    public void pageSearch() throws Exception {

        SearchResultImpl mockSearchResult1 = new SearchResultImpl();
        mockSearchResult1.setTitle("test result 1");

        SearchResultImpl mockSearchResult2 = new SearchResultImpl();
        mockSearchResult2.setTitle("test result 2");

        ArrayList<SearchResult> searchResults = new ArrayList<>();
        searchResults.add(mockSearchResult1);
        searchResults.add(mockSearchResult2);

        SearchResults mockSearchResults = new SearchResults();
        mockSearchResults.setNumberResults(2);
        mockSearchResults.setNumberEstimatedResults(10);
        mockSearchResults.setResults(searchResults);

        Mockito.when(searchService.query(Mockito.any())).thenReturn(mockSearchResults);

        String URI = "/textsearch?q=sapo";

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(URI);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        MockHttpServletResponse response = result.getResponse();
        JSONObject jsonResponse = new JSONObject(response.getContentAsString());
        assertThat(jsonResponse.getInt("totalItems")).isEqualTo(2);
        assertThat(jsonResponse.getString("next_page")).isEqualTo("http://localhost:8080/textsearch?q=sapo&offset=50");

        JSONArray jsonArray = jsonResponse.getJSONArray("response_items");
        assertThat(jsonArray.length()).isEqualTo(2);
        assertThat(jsonArray.getJSONObject(0).getString("title")).isEqualTo("test result 1");
        assertThat(jsonResponse.getInt("estimated_nr_results")).isEqualTo(10);
        assertThat(jsonResponse.getString("serviceName")).isNotBlank();
        assertThat(jsonResponse.getString("linkToService")).isNotBlank();
    }
}