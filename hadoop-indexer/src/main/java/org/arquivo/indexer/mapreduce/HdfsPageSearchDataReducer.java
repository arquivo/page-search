package org.arquivo.indexer.mapreduce;

import com.google.gson.Gson;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.arquivo.indexer.data.PageData;

import java.io.IOException;

public class HdfsPageSearchDataReducer extends Reducer<Text, PageData, NullWritable, Text> {
    private Gson gson;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        this.gson = new Gson();
        super.setup(context);
    }

    @Override
    protected void reduce(Text key, Iterable<PageData> values, Context context) throws IOException, InterruptedException {
        for (PageData value : values) {
            PageData doc = value;
            context.write(NullWritable.get(), new Text(gson.toJson(doc)));
        }
    }
}
