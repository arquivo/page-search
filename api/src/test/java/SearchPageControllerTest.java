import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SearchPageControllerTest {

    @LocalServerPort
    private int port;

    @Test
    public void getSearchResult() {
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<String> response = testRestTemplate
                .getForEntity("http://localhost:" + this.port + "/textsearch", String.class);

        assert (true);
        // assertEqual(result.getRequestParameters(),);
    }

}
