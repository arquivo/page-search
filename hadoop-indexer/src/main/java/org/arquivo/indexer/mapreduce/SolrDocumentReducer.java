package org.arquivo.indexer.mapreduce;

import com.google.gson.Gson;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.arquivo.indexer.data.PageSearchData;

import java.io.IOException;

public class SolrDocumentReducer extends Reducer<Text, PageSearchData, Text, Text> {
    // Do some stuff with a SolrDocument and dump to textual format
    private Gson gson;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        this.gson = new Gson();
        super.setup(context);
    }

    @Override
    protected void reduce(Text key, Iterable<PageSearchData> values, Context context) throws IOException, InterruptedException {
        for (PageSearchData value : values) {
            PageSearchData doc = value;
            context.write(new Text(doc.getId()), new Text(gson.toJson(doc)));

        }
    }
}
