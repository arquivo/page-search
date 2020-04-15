package org.arquivo.indexer.mapreduce;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.arquivo.indexer.data.PageData;

import java.io.IOException;

// identity reducer so we have the same number of partitions has the inverted links
public class PageSearchDataReducer extends Reducer<Text, PageData, Text, PageData> {

    @Override
    protected void reduce(Text key, Iterable<PageData> values, Context context) throws IOException, InterruptedException {
        // TODO think better about this
        // Every page is different since the Key used is timestamp/digest
        for (PageData value : values) {
            context.write(new Text(value.getUrl()), value);
        }
    }
}
