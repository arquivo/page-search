package pt.arquivo.indexer.mapreduce;

import org.apache.hadoop.fs.Path;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class WarcPathFilterTest {

    @Test
    public void accept() {
        WarcPathFilter warcPathFilter = new WarcPathFilter();
        Path validPath1 = new Path("pathtofile/warc.gz");
        Path validPath2 = new Path("pathtofile/arc.gz");
        Path invalidPath1 = new Path("pathtofile/file.txt");

        assertThat(warcPathFilter.accept(validPath1)).isTrue();
        assertThat(warcPathFilter.accept(validPath2)).isTrue();
        assertThat(warcPathFilter.accept(invalidPath1)).isFalse();
    }
}