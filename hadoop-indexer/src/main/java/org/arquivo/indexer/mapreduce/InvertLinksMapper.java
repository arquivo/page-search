package org.arquivo.indexer.mapreduce;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;
import org.arquivo.indexer.data.*;

import java.io.IOException;

public class InvertLinksMapper extends Mapper<Text, PageSearchData, WebArchiveKey, Inlink> {
    private final Logger logger = Logger.getLogger(InvertLinksMapper.class);

    @Override
    protected void map(Text key, PageSearchData value, Context context) throws IOException, InterruptedException {

        Outlink[] outlinks = value.getOutLinks();
        for (int i = 0; i < value.getnOutLinks(); i++){
            Outlink outlink = outlinks[i];
            // FIXME canolize or normalize this link
            String toUrl = outlink.getToUrl();
            String anchorText = outlink.getAnchor();
            String fromUrl = value.getUrl();

            // day level timestamp only
            // TODO this should be configurable maybe? IDEA, we can make a parameter that defines the granurality level
            // So the default would be by day, but special collection it can be set hour level?
            // Or, make it more relaxed, so it does a monthly graph, or just by URL!
            // Make everything the same for now (like one big collection without multiple crawls to the same resource)
            String timestamp = "000000000";
            // String timestamp = value.getTstamp().substring(0, 8);
            Inlink inlink = new Inlink(fromUrl, anchorText);
            logger.info("Generating Inlink from " + fromUrl);

            WebArchiveKey webArchiveKey = new WebArchiveKey(toUrl, Long.parseLong(timestamp));
            context.write(webArchiveKey , inlink);
        }
    }
}
