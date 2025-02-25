package pt.arquivo.services.cdx;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemCDXTest {

    private ItemCDX itemCDX1;
    private ItemCDX itemCDX2;

    @Before
    public void setUp() throws Exception {
        itemCDX1 = new ItemCDX("http://example.com", "201901010101203401", "DHAJKWDAK",
                "text/html", "200", "something", "80", "80", "TESTE");
        itemCDX2 = new ItemCDX("", "201901010101203400", "DHAJKWDA",
                "text/html", "200", "something", "80", "80", "TESTE");
    }

    @Test
    public void testToString() {
        assertThat(itemCDX1.toString()).isEqualTo("ItemCDX [url=http://example.com, timestamp=201901010101203401, digest=DHAJKWDAK, mime=text/html, statusCode=200, filename=something, length=80, offset=80]");
        assertThat(itemCDX1.hashCode()).isEqualTo(-1970849035);
    }

    @Test
    public void testEquals() {
        assertThat(itemCDX1.equals(itemCDX2)).isFalse();
    }

    @Test
    public void checkFields() {
        assertThat(itemCDX1.checkFields()).isTrue();
        assertThat(itemCDX2.checkFields()).isFalse();
    }

}