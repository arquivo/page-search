package org.arquivo.indexer.mapreduce;

import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.ArchiveRecord;
import org.arquivo.indexer.data.PageSearchData;
import org.arquivo.indexer.parsers.WARCParser;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class SolrDocumentMapper extends Mapper<LongWritable, Text, Text, PageSearchData> {
    // maps from an ArchiveRecord to a SolrDocument intermediate format
    private final Logger logger = Logger.getLogger(SolrDocumentMapper.class);
    private WARCParser warcParser;
    private Gson gson;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        Config conf = ConfigFactory.load();
        this.gson = new Gson();
        this.warcParser = new WARCParser(conf);
        super.setup(context);
    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String arcURL = value.toString();
        if (!arcURL.isEmpty()) {
            logger.info("(W)ARCNAME: " + arcURL);
            //context.getCounter(IMAGE_COUNTERS.WARCS).increment(1);

            URL url = null;
            try {
                url = new URL(arcURL);
            } catch (MalformedURLException ignored) {

            }

            String[] surl = url.getPath().split("/");
            String filename = System.currentTimeMillis() + "_" + surl[surl.length - 1];
            File dest = new File("/tmp/" + filename);

            try {
                FileUtils.copyURLToFile(url, dest);
            } catch (IOException e) {
                e.printStackTrace();
                //context.getCounter(IMAGE_COUNTERS.WARCS_DOWNLOAD_ERROR).increment(1);
            }

            ArchiveReader reader = null;
            ArrayList<PageSearchData> listDocs = new ArrayList();
            try {
                reader = ArchiveReaderFactory.get(dest);
                Iterator<ArchiveRecord> ir = reader.iterator();

                int recordCount = 1;
                int lastFailedRecord = 0;

                while (ir.hasNext()) {
                    try {
                        ArchiveRecord rec = ir.next();
                        try {
                            PageSearchData doc = warcParser.extract("teste", rec);
                            if (doc != null) {
                                listDocs.add(doc);
                            }
                            logger.debug(doc);
                        } catch (Exception e) {
                            // TODO log more information about the record that failed.
                            logger.error("Unable to parse record due to unknow reasons", e);
                            continue;
                        } catch (OutOfMemoryError e) {
                            logger.error("Unable to parse record due to out of memory problems", e);
                            continue;
                        }
                    } catch (RuntimeException e) {
                        if (lastFailedRecord != recordCount) {
                            lastFailedRecord = recordCount;
                            continue;
                        }
                        logger.error("Failed to reach next record, last record already on error - skipping the rest of the records");
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (PageSearchData doc : listDocs) {
                // TODO
                // Serialize Object instead of texting it, need to do a class that implesmentes Writable
                context.write(new Text(doc.getId()), doc);
            }

            FileUtils.deleteQuietly(dest);
        }
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        super.cleanup(context);
    }

    @Override
    public void run(Context context) throws IOException, InterruptedException {
        super.run(context);
    }
}
