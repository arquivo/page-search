package org.arquivo.parsers;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.tika.exception.TikaException;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.ArchiveRecord;
import org.arquivo.solr.SolrDocumentWrapper;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class WARCParserTest {

    private WARCParser warcParser;

    @Before
    public void init(){
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
        SolrDocumentWrapper doc = warcParser.extract(reader.getFileName(), rec);

        // ASSERT FIELDS
        assertEquals(doc.getSolrInputDocument().get("tika_content_type").getValue().toString(), "application/gzip");
        assertEquals(doc.getSolrInputDocument().get("title").getValue().toString(), "");
        assertEquals(doc.getSolrInputDocument().get("digest").getValue().toString(), "3129038109");
        assertEquals(doc.getSolrInputDocument().get("encoding").getValue().toString(), "iso-8859-1");
        assertTrue(doc.getSolrInputDocument().get("content").getValue().toString().contains("Forbidden"));
        assertEquals(doc.getSolrInputDocument().get("subType").getValue(), "html");
        assertEquals(doc.getSolrInputDocument().get("primaryType").getValue(), "text");
        assertEquals(doc.getSolrInputDocument().get("type").getValue(), "text/html; charset=iso-8859-1");
        assertEquals(doc.getSolrInputDocument().get("url").getValue(), "http://publico.pt/");
        assertEquals(doc.getSolrInputDocument().get("id").getValue(), "19961013180344/FhcAj9+g5IrKjL+HJyyf3g==");
        assertEquals(doc.getSolrInputDocument().get("domain").getValue().toString(), "publico.pt");
        assertEquals(doc.getSolrInputDocument().get("host").getValue(), "publico.pt");
        assertEquals(doc.getSolrInputDocument().getField("tstamp").getValue(), "19961013180344");
    }
}