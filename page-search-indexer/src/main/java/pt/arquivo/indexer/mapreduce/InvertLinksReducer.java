package pt.arquivo.indexer.mapreduce;

import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;
import pt.arquivo.indexer.data.Inlink;
import pt.arquivo.indexer.data.Inlinks;
import pt.arquivo.indexer.data.WebArchiveKey;

import java.io.IOException;

public class InvertLinksReducer extends Reducer<WebArchiveKey, Inlink, WebArchiveKey, Inlinks> {

    private final Logger logger = Logger.getLogger(InvertLinksMapper.class);

    @Override
    protected void reduce(WebArchiveKey key, Iterable<Inlink> values, Context context) throws IOException, InterruptedException {
        Inlinks inlinks = new Inlinks();
        for (Inlink inlink : values){
            inlinks.add(inlink);
        }
        logger.info("Found "  + inlinks.size() + " inlinks to URL " + "(" + key.getTimeStamp() + "#" + key.getUrl() + ")");
        context.write(key, inlinks);
    }
}
