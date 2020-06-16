package pt.arquivo.indexer.mapreduce;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;
import pt.arquivo.indexer.data.Inlink;
import pt.arquivo.indexer.data.Outlink;
import pt.arquivo.indexer.data.PageData;
import pt.arquivo.indexer.data.WebArchiveKey;
import pt.arquivo.indexer.parsers.WARCParser;

import java.io.IOException;

public class InvertLinksMapper extends Mapper<WebArchiveKey, PageData, WebArchiveKey, Inlink> {
    private final Logger logger = Logger.getLogger(InvertLinksMapper.class);
    private int graphTimeSlice;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        String graphTimePolicy = context.getConfiguration().get("GraphTimeSlice", "none");
        this.graphTimeSlice = WebArchiveKey.keyTimeSlice(graphTimePolicy);
        super.setup(context);
    }

    @Override
    protected void map(WebArchiveKey key, PageData value, Context context) throws IOException, InterruptedException {

        Outlink[] outlinks = value.getOutLinks();
        for (int i = 0; i < value.getnOutLinks(); i++) {
            Outlink outlink = outlinks[i];

            String toUrl = outlink.getToUrl();
            String anchorText = outlink.getAnchor();
            String fromUrl = value.getUrl();

            String keyTimeStamp = value.getTstamp().substring(0, graphTimeSlice);
            Inlink inlink = new Inlink(fromUrl, anchorText);

            WebArchiveKey webArchiveKey = new WebArchiveKey(WARCParser.canocalizeSurtUrl(toUrl), keyTimeStamp);
            context.write(webArchiveKey, inlink);
            logger.info("Generating Inlink from " + webArchiveKey.getTimeStamp() + "#" + webArchiveKey.getUrl()  + " to " + toUrl);
        }
    }
}
