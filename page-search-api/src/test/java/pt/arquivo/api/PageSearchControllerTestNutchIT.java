package pt.arquivo.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {TestNutchConfig.class, PageSearchApplication.class})
public class PageSearchControllerTestNutchIT {

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
    public void TestTextSearchEndpointOffsets() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        String uri = "/textsearch?q=sapo";

        ResponseEntity<String> response = testRestTemplate.exchange(
                "http://localhost:" + port + uri,
                HttpMethod.GET, entity, String.class
        );

        final JSONObject jsonResponse = new JSONObject(response.getBody());
        assertThatExceptionOfType(JSONException.class).isThrownBy(() -> {
            jsonResponse.getString("next_page");
        });
        JSONArray jsonArray = jsonResponse.getJSONArray("response_items");
        assertThat(jsonArray.getJSONObject(0).getString("title")).isEqualTo("SAPO, Servidor de Apontadores Portugueses");

        uri = "/textsearch?q=ua";
        response = testRestTemplate.exchange(
                "http://localhost:" + port + uri,
                HttpMethod.GET, entity, String.class
        );
        JSONObject jsonResponse2 = new JSONObject(response.getBody());
        assertThat(jsonResponse2.getString("next_page")).isNotBlank();

        uri = "/textsearch?q=ua&offset=50";
        response = testRestTemplate.exchange(
                "http://localhost:" + port + uri,
                HttpMethod.GET, entity, String.class
        );
        jsonResponse2 = new JSONObject(response.getBody());
        assertThat(jsonResponse2.getString("previous_page")).isNotBlank();
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

        // Test bad resource id
        String badResourceUri = "/textextracted?m=http://idontexist.pt//19961013150238";
        ResponseEntity<String> notFoundResponse = testRestTemplate.exchange(
                "http://localhost:" + port + badResourceUri,
                HttpMethod.GET, entity, String.class
        );
        assertThat(notFoundResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

//     @Test
//     public void TestMetadataEndpoint() throws Exception {
//         // Test /textsearch?metadata request
//         HttpEntity<String> entity = new HttpEntity<>(null, headers);
//         String uri = "/textsearch?metadata=http://sapo.ua.pt//19961013150238";

//         ResponseEntity<String> response = testRestTemplate.exchange(
//                 "http://localhost:" + port + uri,
//                 HttpMethod.GET, entity, String.class
//         );

//         JSONObject jsonResponse = new JSONObject(response.getBody());
//         JSONArray jsonArray = jsonResponse.getJSONArray("response_items");
//         assertThat(jsonArray.getJSONObject(0).getString("title"))
//                 .isEqualTo("SAPO, Servidor de Apontadores Portugueses");
//         assertThat(jsonArray.getJSONObject(0).getString("tstamp")).isEqualTo("19961013150238");

//         // Test /metadata endpoint (future endpoint)
//         uri = "/metadata?id=http://sapo.ua.pt//19961013150238";
//         response = testRestTemplate.exchange(
//                 "http://localhost:" + port + uri,
//                 HttpMethod.GET, entity, String.class
//         );

//         jsonResponse = new JSONObject(response.getBody());
//         jsonArray = jsonResponse.getJSONArray("response_items");
//         assertThat(jsonArray.getJSONObject(0).getString("title"))
//                 .isEqualTo("SAPO, Servidor de Apontadores Portugueses");
//         assertThat(jsonArray.getJSONObject(0).getString("tstamp")).isEqualTo("19961013150238");
//     }
}
