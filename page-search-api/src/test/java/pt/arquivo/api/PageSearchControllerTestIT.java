package pt.arquivo.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {TestNutchConfig.class, PageSearchApplication.class})
public class PageSearchControllerTestIT {

    TestRestTemplate testRestTemplate = new TestRestTemplate();
    HttpHeaders headers = new HttpHeaders();

    @LocalServerPort
    private int port;

    @Test
    public void TestTextSearchEndpoint() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        String uri = "/textsearch?q=sapo";

        ResponseEntity<String> response = testRestTemplate.exchange(
                "http://localhost:" + port + uri,
                HttpMethod.GET, entity, String.class
        );

        JSONObject jsonResponse = new JSONObject(response.getBody());
        JSONArray jsonArray = jsonResponse.getJSONArray("response_items");
        assertThat(jsonArray.getJSONObject(0).getString("title")).isEqualTo("SAPO, Servidor de Apontadores Portugueses");
    }

    @Test
    public void TestTextExtractedEndpoint() {
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        String uri = "/textextracted?m=http://sapo.ua.pt//19961013150238";

        ResponseEntity<String> response = testRestTemplate.exchange(
                "http://localhost:" + port + uri,
                HttpMethod.GET, entity, String.class
        );
        assertThat(response.getBody()).startsWith("SAPO");
    }

    @Test
    public void TestMetadataEndpoint() throws Exception {
        // Test /textsearch?metadata request
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        String uri = "/textsearch?metadata=http://sapo.ua.pt//19961013150238";

        ResponseEntity<String> response = testRestTemplate.exchange(
                "http://localhost:" + port + uri,
                HttpMethod.GET, entity, String.class
        );

        JSONObject jsonResponse = new JSONObject(response.getBody());
        JSONArray jsonArray = jsonResponse.getJSONArray("response_items");
        assertThat(jsonArray.getJSONObject(0).getString("title"))
                .isEqualTo("SAPO, Servidor de Apontadores Portugueses");
        assertThat(jsonArray.getJSONObject(0).getString("tstamp")).isEqualTo("19961013150238");

        // Test /metadata endpoint (future endpoint)
        uri = "/metadata?id=http://sapo.ua.pt//19961013150238";
        response = testRestTemplate.exchange(
                "http://localhost:" + port + uri,
                HttpMethod.GET, entity, String.class
        );

        jsonResponse = new JSONObject(response.getBody());
        jsonArray = jsonResponse.getJSONArray("response_items");
        assertThat(jsonArray.getJSONObject(0).getString("title"))
                .isEqualTo("SAPO, Servidor de Apontadores Portugueses");
        assertThat(jsonArray.getJSONObject(0).getString("tstamp")).isEqualTo("19961013150238");
    }
}
