package pt.arquivo.indexer.data;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InlinkTest {

    @Test
    public void testEquals() {
        Inlink inlink1 = new Inlink("site.com", "anchor text");
        Inlink inlink2 = new Inlink("site.com", "anchor text");
        Inlink inlink3 = new Inlink("site.com", "something else");
        assertThat(inlink1.equals(inlink2)).isTrue();
        assertThat(inlink1.equals(inlink3)).isFalse();

        Object obj = new Object();
        assertThat(inlink1.equals(obj)).isFalse();
    }
}