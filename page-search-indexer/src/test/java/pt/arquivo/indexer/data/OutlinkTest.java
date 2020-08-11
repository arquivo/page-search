package pt.arquivo.indexer.data;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OutlinkTest {

    @Test
    public void testEquals() {
        Outlink outlink1 = new Outlink("site.com", "anchor text");
        Outlink outlink2 = new Outlink("site.com", "anchor text");
        Outlink outlink3 = new Outlink("site.com", "something else");
        assertThat(outlink1.equals(outlink2)).isTrue();
        assertThat(outlink1.equals(outlink3)).isFalse();

        Object obj = new Object();
        assertThat(outlink1.equals(obj)).isFalse();
    }
}