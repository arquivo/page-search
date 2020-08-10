package pt.arquivo.indexer.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;


public class PageSearchIndexingTest {
    private Configuration conf;
    private Path output;

    @Before
    public void setup() throws IOException {
        conf = new Configuration();
        conf.set("fs.default.name", "file:///");
        conf.set("mapred.job.tracker", "local");
        conf.set("collection", "TESTE");
        output = new Path("target/output");
        FileSystem fs = FileSystem.getLocal(conf);
        fs.delete(output, true);
    }

    private void runMapReduceJob(Tool job, Configuration conf, String[] args) throws Exception {
        job.setConf(conf);
        assertThat(job.run(args)).isEqualTo(0);
    }

    @Test
    public void testPageSearchIndexingWorkflow() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("chunked.gzip.html.record.warc.gz").getFile());
        Path input = new Path(file.getAbsolutePath());

        runMapReduceJob(new HdfsPageSearchDataDriver(), conf, new String[]{input.toString(), output.toString()});
        runMapReduceJob(new InvertLinksDriver(), conf, new String[]{output.toString()});
        runMapReduceJob(new SolrPageDocDriver(), conf, new String[]{output.toString()});

        JSONParser jsonParser = new JSONParser();
        Object obj = jsonParser.parse(new FileReader("target/output/solr_data/part-r-00000"));
        JSONObject jsonObject = (JSONObject)  obj;
        assertThat(jsonObject).isNotNull();

        assertThat(jsonObject.get("primary_type")).isEqualTo("text");
        assertThat(jsonObject.get("warc_name")).isEqualTo("chunked.gzip.html.record.warc.gz");
        assertThat(jsonObject.get("collection")).isEqualTo("TESTE");
        assertThat(jsonObject.get("surt_url")).isEqualTo("(pt,publico,");
        assertThat(jsonObject.get("type")).isEqualTo("text/html");
        assertThat(jsonObject.get("encoding")).isEqualTo("");
        assertThat(jsonObject.get("content")).isEqualTo("Error 403 Forbidden - URL containing .. forbidden [don't try to break in] CERN httpd 3.0 Internet URL- http://www.publico.pt:80/palop/..");
        assertThat(jsonObject.get("url")).isEqualTo("http://publico.pt/");
        assertThat(jsonObject.get("warc_offset")).isEqualTo(0L);
        assertThat(jsonObject.get("site")).isEqualTo("publico.pt");
        assertThat(jsonObject.get("inlinks")).isEqualTo(1L);
        assertThat(jsonObject.get("tstamp")).isEqualTo("19961013180344");
        assertThat(jsonObject.get("sub_type")).isEqualTo("html");
        assertThat(jsonObject.get("anchor")).isEqualTo("http://www.publico.pt:80/palop/.. ");
        // TODO verify if the digest is being calculated rightly (check with the warc recrod)
        assertThat(jsonObject.get("digest")).isEqualTo("E280C2155BCE89C5E6DAAD68C44FC96F");
        assertThat(jsonObject.get("outlinks")).isEqualTo(2L);
        assertThat(jsonObject.get("id")).isEqualTo("19961013180344/FhcAj9+g5IrKjL+HJyyf3g==");
        assertThat(jsonObject.get("content_length")).isEqualTo(265L);
    }
}