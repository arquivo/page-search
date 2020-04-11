package org.arquivo.indexer.mapreduce;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;
import org.archive.io.ArchiveRecord;
import org.arquivo.indexer.data.PageSearchData;
import org.arquivo.indexer.data.WritableArchiveRecord;
import org.arquivo.indexer.parsers.WARCParser;

import java.io.IOException;
import java.util.ArrayList;

public class HdfsPageSearchDataMapper extends Mapper<LongWritable, WritableArchiveRecord, Text, PageSearchData> {
    // maps from an ArchiveRecord to a intermediate format
    private final Logger logger = Logger.getLogger(HdfsPageSearchDataMapper.class);
    private WARCParser warcParser;

    enum PagesCounters {
        RECORDS_COUNT,
        RECORDS_ACCEPTED_COUNT,
        RECORDS_DISCARDED_COUNT,
        RECORDS_DISCARDED_ERROR_COUNT
    }

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        Config conf = ConfigFactory.load();
        this.warcParser = new WARCParser(conf);
        super.setup(context);
    }

    @Override
    protected void map(LongWritable key, WritableArchiveRecord value, Context context) throws IOException, InterruptedException {
        ArrayList<PageSearchData> listDocs = new ArrayList();
        ArchiveRecord rec = value.getRecord();
        context.getCounter(PagesCounters.RECORDS_COUNT).increment(1);
        try {
            // TODO get warcName
            PageSearchData doc = warcParser.extract("", rec);
            if (doc != null) {
                logger.info("Processing Record with URL: ".concat(doc.getUrl()));
                doc.setCollection(context.getConfiguration().get("collection", ""));
                listDocs.add(doc);
                context.getCounter(PagesCounters.RECORDS_ACCEPTED_COUNT).increment(1);
            } else {
                context.getCounter(PagesCounters.RECORDS_DISCARDED_COUNT).increment(1);
            }
        } catch (Exception e) {
            logger.error("Unable to parse record due to unknow reasons", e);
            context.getCounter(PagesCounters.RECORDS_DISCARDED_ERROR_COUNT).increment(1);
        } catch (OutOfMemoryError e) {
            context.getCounter(PagesCounters.RECORDS_DISCARDED_ERROR_COUNT).increment(1);
            logger.error("Unable to parse record due to out of memory problems", e);
        }

        for (PageSearchData doc : listDocs) {
            context.write(new Text(doc.getId()), doc);
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
