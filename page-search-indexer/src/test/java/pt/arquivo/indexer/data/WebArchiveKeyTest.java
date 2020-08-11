package pt.arquivo.indexer.data;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class WebArchiveKeyTest {

    @Test
    public void keyTimeSlice() {
        assertThat(WebArchiveKey.keyTimeSlice("none")).isEqualTo(0);
        assertThat(WebArchiveKey.keyTimeSlice("year")).isEqualTo(4);
        assertThat(WebArchiveKey.keyTimeSlice("monthly")).isEqualTo(6);
        assertThat(WebArchiveKey.keyTimeSlice("daily")).isEqualTo(8);
        assertThatThrownBy(() -> {
            WebArchiveKey.keyTimeSlice("nosense");
        }).isInstanceOf(IllegalArgumentException.class);
    }
}