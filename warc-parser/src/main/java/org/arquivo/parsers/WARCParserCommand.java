package org.arquivo.parsers;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.ArchiveRecord;
import org.arquivo.solr.SolrDocumentWrapper;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class WARCParserCommand {

    private static Log log = LogFactory.getLog(WARCParser.class);

    public static void processArchive(WARCParser warcParser, File file) {
        ArchiveReader reader = null;
        try {
            reader = ArchiveReaderFactory.get(file);
            Iterator<ArchiveRecord> ir = reader.iterator();

            int recordCount = 1;
            int lastFailedRecord = 0;

            while (ir.hasNext()) {
                try {
                    ArchiveRecord rec = ir.next();
                    try {
                        SolrDocumentWrapper doc = warcParser.extract("teste", rec);

                        // move this code
                        System.out.println(doc.getSolrInputDocument());
                        // solrClient.add(doc.getSolrInputDocument());
                        // solrClient.commit();

                    } catch (Exception e) {
                        continue;
                    } catch (OutOfMemoryError e) {
                        continue;
                    }
                } catch (RuntimeException e) {
                    if (lastFailedRecord != recordCount) {
                        lastFailedRecord = recordCount;
                        continue;
                    }
                    log.error("Failed to reach next record, last record already on error - skipping the rest of the records");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        Config conf = ConfigFactory.load();
        WARCParser warcParser = new WARCParser(conf);

        // receive input folder with warcs there
        if (args.length < 1) {
            System.out.print("Specify folder with WARCs to parse.");
            System.exit(0);
        } else {
            String folderpath = args[0];
            File dir = new File(folderpath);
            File[] directoryListing = dir.listFiles();
            if (directoryListing != null) {
                for (File child : directoryListing) {
                    if (child.isFile()) {
                        String fileName = child.getName();
                        if (fileName.endsWith("warc.gz") || fileName.endsWith("arc.gz") || fileName.endsWith("warc") ||
                                fileName.endsWith("arc")) {
                            processArchive(warcParser, child);
                        }
                    }
                }
            }

        }
    }
}
