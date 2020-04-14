package org.arquivo.indexer.mapreduce;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.arquivo.indexer.data.PageSearchData;

import java.io.IOException;

// identity reducer so we have the same number of partitions has the inverted links
public class PageSearchDataReducer extends Reducer<Text, PageSearchData, Text, PageSearchData> {

    @Override
    protected void reduce(Text key, Iterable<PageSearchData> values, Context context) throws IOException, InterruptedException {
        // TODO think better about this
        // Every page is different since the Key used is timestamp/digest
        for (PageSearchData value : values) {
            context.write(new Text(value.getUrl()), value);
        }
    }
}
