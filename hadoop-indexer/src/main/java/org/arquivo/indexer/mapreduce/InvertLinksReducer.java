package org.arquivo.indexer.mapreduce;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.arquivo.indexer.data.Inlink;
import org.arquivo.indexer.data.Inlinks;
import org.arquivo.indexer.data.WebArchiveKey;

import java.io.IOException;

public class InvertLinksReducer extends Reducer<WebArchiveKey, Inlink, WebArchiveKey, Inlinks> {

    @Override
    protected void reduce(WebArchiveKey key, Iterable<Inlink> values, Context context) throws IOException, InterruptedException {
        Inlinks inlinks = new Inlinks();
        for (Inlink inlink : values){
            inlinks.add(inlink);
        }
        context.write(key, inlinks);
    }
}
