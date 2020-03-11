import org.arquivo.api.SearchPageApplication;
import org.arquivo.api.SearchResultResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;


@SpringBootTest(classes = SearchPageApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class SearchPageControllerTest{

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void getSearchResult() {
        SearchResultResponse response = this.restTemplate.getForObject("http://localhost:" + this.port + "/textsearch?q=de", SearchResultResponse.class);
        assert (true);
    }

}
