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
import pt.arquivo.services.SearchResultNutchImpl;
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
    public void testSearchBySiteDefaultDedup() throws Exception {
        SearchResultNutchImpl mockSearchResult1 = new SearchResultNutchImpl();
        SearchResults mockSearchResults = new SearchResults();

        ArrayList<SearchResult> searchResults = new ArrayList<>();
        searchResults.add(mockSearchResult1);

        mockSearchResults.setResults(searchResults);

        Mockito.when(searchService.query(Mockito.any())).thenReturn(mockSearchResults);

        String URI = "/textsearch?q=sapo&prettyPrint=true&siteSearch=example.com";
        String URI2 = "/textsearch?q=sapo&prettyPrint=true";
        String URI3 = "/textsearch?q=sapo&prettyPrint=true&siteSearch=example.com&dedupField=site";

        // test searchSite default dedupField
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(URI);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        MockHttpServletResponse response = result.getResponse();
        JSONObject jsonResponse = new JSONObject(response.getContentAsString());
        assertThat(jsonResponse.getJSONObject("request_parameters").getString("dedupField")).isEqualTo("url");

        // test default dedupField when query is by site
        requestBuilder = MockMvcRequestBuilders.get(URI2);
        result = mockMvc.perform(requestBuilder).andReturn();

        response = result.getResponse();

        jsonResponse = new JSONObject(response.getContentAsString());
        assertThat(jsonResponse.getJSONObject("request_parameters").getString("dedupField")).isEqualTo("site");

        // test custom dedupField on a search by site query
        requestBuilder = MockMvcRequestBuilders.get(URI3);
        result = mockMvc.perform(requestBuilder).andReturn();

        response = result.getResponse();

        jsonResponse = new JSONObject(response.getContentAsString());
        assertThat(jsonResponse.getJSONObject("request_parameters").getString("dedupField")).isEqualTo("site");
    }

    @Test
    public void pageSearchOffset() throws Exception {
        SearchResultNutchImpl mockSearchResult1 = new SearchResultNutchImpl();
        SearchResultNutchImpl mockSearchResult2 = new SearchResultNutchImpl();
        SearchResultNutchImpl mockSearchResult3 = new SearchResultNutchImpl();

        mockSearchResult1.setTitle("test result 1");
        mockSearchResult2.setTitle("test result 2");
        mockSearchResult3.setTitle("test result 3");

        ArrayList<SearchResult> searchResults = new ArrayList<>();
        searchResults.add(mockSearchResult1);
        searchResults.add(mockSearchResult2);
        searchResults.add(mockSearchResult3);

        SearchResults mockSearchResults = new SearchResults();
        mockSearchResults.setNumberResults(3);
        mockSearchResults.setEstimatedNumberResults(10);
        mockSearchResults.setResults(searchResults);

        Mockito.when(searchService.query(Mockito.any())).thenReturn(mockSearchResults);

        String URI = "/textsearch?q=sapo&offset=2&maxItems=1";

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(URI);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();


        MockHttpServletResponse response = result.getResponse();
        JSONObject jsonResponse = new JSONObject(response.getContentAsString());
        assertThat(jsonResponse.getString("next_page")).isEqualTo("http://localhost:8081/textsearch?q=sapo&offset=3&maxItems=1");
    }

    @Test
    public void pageSearch() throws Exception {
        /* Mainly verify API specification */

        SearchResultNutchImpl mockSearchResult1 = new SearchResultNutchImpl();
        mockSearchResult1.setTitle("test result 1");

        // just to force result spec verification below
        mockSearchResult1.setContentLength(3819);
        mockSearchResult1.setCollection("TESTE");
        mockSearchResult1.setTimeStamp("20192010");
        mockSearchResult1.setLinkToMetadata("URL");
        mockSearchResult1.setLinkToOriginalFile("URL");
        mockSearchResult1.setLinkToExtractedText("URL");
        mockSearchResult1.setLinkToArchive("URL");
        mockSearchResult1.setLinkToNoFrame("URL");
        mockSearchResult1.setLinkToScreenshot("URL");
        mockSearchResult1.setMimeType("text/html");
        mockSearchResult1.setDigest("0eae260a4bf611de89cf2eb96db21ba9");
        mockSearchResult1.setOffset(0);
        mockSearchResult1.setFileName("teste.arc.gz");
        mockSearchResult1.setStatusCode(200);
        mockSearchResult1.setSnippet("SNIPPET DE TESTE");
        mockSearchResult1.setDate("some epocho number");
        mockSearchResult1.setEncoding("utf-8");

        SearchResultNutchImpl mockSearchResult2 = new SearchResultNutchImpl();
        mockSearchResult2.setTitle("test result 2");

        // just to force result spec verification below
        mockSearchResult2.setContentLength(3819);
        mockSearchResult2.setCollection("TESTE");
        mockSearchResult2.setTimeStamp("20192010");
        mockSearchResult2.setLinkToMetadata("URL");
        mockSearchResult2.setLinkToOriginalFile("URL");
        mockSearchResult2.setLinkToExtractedText("URL");
        mockSearchResult2.setLinkToArchive("URL");
        mockSearchResult2.setLinkToNoFrame("URL");
        mockSearchResult2.setLinkToScreenshot("URL");
        mockSearchResult2.setMimeType("text/html");
        mockSearchResult2.setDigest("0eae260a4bf611de89cf2eb96db21ba9");
        mockSearchResult2.setOffset(0);
        mockSearchResult2.setFileName("teste.arc.gz");
        mockSearchResult2.setStatusCode(200);
        mockSearchResult2.setSnippet("SNIPPET DE TESTE");
        mockSearchResult2.setDate("some epocho number");
        mockSearchResult2.setEncoding("utf-8");

        ArrayList<SearchResult> searchResults = new ArrayList<>();
        searchResults.add(mockSearchResult1);
        searchResults.add(mockSearchResult2);

        SearchResults mockSearchResults = new SearchResults();
        mockSearchResults.setNumberResults(2);
        mockSearchResults.setEstimatedNumberResults(10);
        mockSearchResults.setResults(searchResults);

        Mockito.when(searchService.query(Mockito.any())).thenReturn(mockSearchResults);

        String URI = "/textsearch?q=sapo&prettyPrint=true";

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(URI);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        MockHttpServletResponse response = result.getResponse();
        JSONObject jsonResponse = new JSONObject(response.getContentAsString());
        assertThat(jsonResponse.getString("next_page")).isEqualTo("http://localhost:8081/textsearch?q=sapo&prettyPrint=true&offset=50");
        assertThat(jsonResponse.getString("serviceName")).isNotBlank();
        assertThat(jsonResponse.getString("linkToService")).isNotBlank();
        assertThat(jsonResponse.getInt("estimated_nr_results")).isEqualTo(10);

        // verify request_parameters
        JSONObject jsonRequests = jsonResponse.getJSONObject("request_parameters");
        assertThat(jsonRequests.getString("q")).isEqualTo("sapo");
        assertThat(jsonRequests.getInt("maxItems")).isEqualTo(50);
        assertThat(jsonRequests.getInt("offset")).isEqualTo(0);
        assertThat(jsonRequests.getInt("dedupValue")).isEqualTo(2);
        assertThat(jsonRequests.getString("dedupField")).isEqualTo("site");


        JSONArray jsonArray = jsonResponse.getJSONArray("response_items");
        assertThat(jsonArray.length()).isEqualTo(2);

        // verify api result spec
        assertThat(jsonArray.getJSONObject(0).getString("title")).isEqualTo("test result 1");
        assertThat(jsonArray.getJSONObject(0).getString("tstamp")).isNotBlank();
        assertThat(jsonArray.getJSONObject(0).getString("contentLength")).isNotBlank();
        assertThat(jsonArray.getJSONObject(0).getString("digest")).isNotBlank();
        assertThat(jsonArray.getJSONObject(0).getString("mimeType")).isNotBlank();
        assertThat(jsonArray.getJSONObject(0).getString("encoding")).isNotBlank();
        assertThat(jsonArray.getJSONObject(0).getString("date")).isNotBlank();
        assertThat(jsonArray.getJSONObject(0).getString("linkToScreenshot")).isNotBlank();
        assertThat(jsonArray.getJSONObject(0).getString("linkToNoFrame")).isNotBlank();
        assertThat(jsonArray.getJSONObject(0).getString("linkToOriginalFile")).isNotBlank();
        assertThat(jsonArray.getJSONObject(0).getString("snippet")).isNotBlank();
        assertThat(jsonArray.getJSONObject(0).getString("fileName")).isNotBlank();
        assertThat(jsonArray.getJSONObject(0).getString("collection")).isNotBlank();
        assertThat(jsonArray.getJSONObject(0).getString("offset")).isNotBlank();

        // verify pretty print
        response.getContentAsString().contains("{\n");
    }
}