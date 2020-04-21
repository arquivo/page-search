package pt.arquivo.indexer;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.tika.exception.TikaException;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.ArchiveRecord;
import pt.arquivo.indexer.data.PageData;
import pt.arquivo.indexer.parsers.WARCParser;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WARCParserTest {

    private WARCParser warcParser;

    @Before
    public void init() {
        Config conf = ConfigFactory.load();
        this.warcParser = new WARCParser(conf);
    }


    @Test
    public void extract() throws IOException, NoSuchAlgorithmException, TikaException, SAXException {
        ClassLoader classLoader = getClass().getClassLoader();
        String path = classLoader.getResource("chunked.gzip.html.record.warc").getPath();

        ArchiveReader reader = ArchiveReaderFactory.get(path);
        Iterator<ArchiveRecord> ir = reader.iterator();

        ArchiveRecord rec = ir.next();
        PageData doc = warcParser.extract(reader.getFileName(), rec);

        // ASSERT FIELDS
        assertEquals(doc.getTikaContentType(), "application/gzip");
        assertEquals(doc.getTitle(), "");
        // assertEquals(doc.getSolrInputDocument().get("digest").getValue().toString(), "3129038109");
        // assertEquals(doc.getSolrInputDocument().get("encoding").getValue().toString(), "iso-8859-1");
        assertTrue(doc.getContent().contains("Forbidden"));
        assertEquals("html", doc.getSubType());
        assertEquals(doc.getPrimaryType(), "text");
        assertEquals(doc.getType(), "text/html");
        assertEquals(doc.getUrl(), "http://publico.pt/");
        assertEquals(doc.getId(), "19961013180344/FhcAj9+g5IrKjL+HJyyf3g==");
        // assertEquals(doc.getSolrInputDocument().get("domain").getValue().toString(), "publico.pt");
        assertEquals(doc.getHost(), "publico.pt");
        assertEquals(doc.getTstamp(), ("19961013180344"));
    }
}