package org.arquivo.indexer;

import com.google.gson.Gson;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.arquivo.solr.SolrDocumentWrapper;
import org.arquivo.solr.SolrFields;

import java.io.IOException;

public class SolrDocumentReducer extends Reducer<Text, Text, Text, Text> {
    // Do some stuff with a SolrDocument and dump to textual format
    private Gson gson;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        this.gson = new Gson();
        super.setup(context);
    }

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        for (Text text : values) {
            SolrDocumentWrapper doc = gson.fromJson(text.toString(), SolrDocumentWrapper.class);
            context.write(new Text((String) doc.getSolrInputDocument().getFieldValue(SolrFields.ID)), new Text(gson.toJson(doc.getSolrInputDocument())));

        }
    }
}
