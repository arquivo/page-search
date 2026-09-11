package pt.arquivo.utils;

import org.apache.commons.httpclient.Header;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HTTPHeaderTest {

    @Test
    public void getHeader_findsValueCaseInsensitively() {
        HTTPHeader headers = new HTTPHeader();
        headers.add(new Header("Content-Type", "text/html"));

        assertThat(headers.getHeader("content-type", "default")).isEqualTo("text/html");
        assertThat(headers.getHeader("CONTENT-TYPE", "default")).isEqualTo("text/html");
    }

    @Test
    public void getHeader_returnsDefaultWhenHeaderMissing() {
        HTTPHeader headers = new HTTPHeader();
        headers.add(new Header("Content-Type", "text/html"));

        assertThat(headers.getHeader("x-missing", "default")).isEqualTo("default");
    }

    @Test
    public void addAll_appendsArrayOfHeaders() {
        HTTPHeader headers = new HTTPHeader();
        headers.addAll(new Header[] { new Header("A", "1"), new Header("B", "2") });

        assertThat(headers).hasSize(2);
        assertThat(headers.getHeader("a", null)).isEqualTo("1");
        assertThat(headers.getHeader("b", null)).isEqualTo("2");
    }

    @Test
    public void httpStatus_isSettableAndGettable() {
        HTTPHeader headers = new HTTPHeader();
        headers.setHttpStatus("200");

        assertThat(headers.getHttpStatus()).isEqualTo("200");
    }
}
