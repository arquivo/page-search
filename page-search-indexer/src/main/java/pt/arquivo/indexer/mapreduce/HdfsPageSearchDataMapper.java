package pt.arquivo.indexer.mapreduce;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;
import org.archive.format.warc.WARCConstants;
import org.archive.io.ArchiveRecord;
import org.archive.io.arc.ARCRecord;
import pt.arquivo.indexer.data.PageData;
import pt.arquivo.indexer.data.WebArchiveKey;
import pt.arquivo.indexer.data.WritableArchiveRecord;
import pt.arquivo.indexer.parsers.WARCParser;

import java.io.IOException;
import java.util.ArrayList;

public class HdfsPageSearchDataMapper extends Mapper<LongWritable, WritableArchiveRecord, WebArchiveKey, PageData> {

    // maps from an ArchiveRecord to a intermediate format
    private final Logger logger = Logger.getLogger(HdfsPageSearchDataMapper.class);
    private WARCParser warcParser;
    private int graphTimeSlice;

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
        String graphTimePolicy = context.getConfiguration().get("GraphTimeSlice", "none");
        this.graphTimeSlice = WebArchiveKey.keyTimeSlice(graphTimePolicy);
        super.setup(context);
    }

    @Override
    protected void map(LongWritable key, WritableArchiveRecord value, Context context) throws IOException, InterruptedException {
        ArrayList<PageData> listDocs = new ArrayList();
        ArchiveRecord rec = value.getRecord();
        context.getCounter(PagesCounters.RECORDS_COUNT).increment(1);
        try {
            PageData doc = warcParser.extract("", rec);
            if (doc != null) {
                logger.info("Processing Record with URL: ".concat(doc.getUrl()));
                doc.setCollection(context.getConfiguration().get("collection", ""));

                if (rec instanceof ARCRecord) {
                    doc.setWarcName(((ARCRecord) rec).getMetaData().getArc());
                } else {
                    doc.setWarcName((String) rec.getHeader().getHeaderValue(WARCConstants.READER_IDENTIFIER_FIELD_KEY));
                }

                doc.setWarcOffset(rec.getHeader().getOffset());

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

        for (PageData doc : listDocs) {
            WebArchiveKey webArchiveKey = new WebArchiveKey(doc.getUrl(), doc.getTstamp().substring(0, graphTimeSlice));
            context.write(webArchiveKey, doc);
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
