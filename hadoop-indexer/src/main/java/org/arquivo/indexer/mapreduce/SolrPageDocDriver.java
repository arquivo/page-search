package org.arquivo.indexer.mapreduce;

import com.google.gson.Gson;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.arquivo.indexer.data.Inlinks;
import org.arquivo.indexer.data.PageSearchData;

import java.io.IOException;

public class SolrPageDocDriver extends Configured implements Tool {

    public static final String DIR_NAME = "solr_data";

    public static class PageDataMapper extends Mapper<Text, PageSearchData, Text, ObjectWritable> {
        @Override
        protected void map(Text key, PageSearchData value, Context context) throws IOException, InterruptedException {
            context.write(key, new ObjectWritable(value));
        }
    }

    public static class InvertedLinksMapper extends Mapper<Text, Inlinks, Text, ObjectWritable> {

        @Override
        protected void map(Text key, Inlinks value, Context context) throws IOException, InterruptedException {
            context.write(key, new ObjectWritable(value));
        }
    }

    public static class SolrPageDocReducer extends Reducer<Text, ObjectWritable, NullWritable, Text> {

        private Gson gson;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            this.gson = new Gson();
            super.setup(context);
        }

        @Override
        protected void reduce(Text key, Iterable<ObjectWritable> values, Context context) throws IOException, InterruptedException {
            PageSearchData pagedata = null;
            Inlinks inlinks = null;
            for (ObjectWritable objectWritable : values) {
                if (objectWritable.get() instanceof PageSearchData) {
                    pagedata = (PageSearchData) objectWritable.get();
                }
                if (objectWritable.get() instanceof Inlinks) {
                    inlinks = (Inlinks) objectWritable.get();
                }
            }
            if (pagedata != null) {
                if (inlinks != null) {
                    pagedata.setInLinks(inlinks);
                    pagedata.setnInLinks(inlinks.size());
                }
                context.write(NullWritable.get(), new Text(gson.toJson(pagedata)));
            }
        }
    }

    @Override
    public int run(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.printf("Usage: %s [generic options] <input>\n", getClass().getSimpleName());
            ToolRunner.printGenericCommandUsage(System.err);
            return -1;
        }

        Configuration conf = getConf();
        String jobName = conf.get("jobName", "CreateSolrDocs");

        Job job = Job.getInstance(conf);
        job.setJarByClass(getClass());

        MultipleInputs.addInputPath(job, new Path(args[0], PageSearchData.DIR_NAME), SequenceFileInputFormat.class, PageDataMapper.class);
        MultipleInputs.addInputPath(job, new Path(args[0], Inlinks.DIR_NAME), SequenceFileInputFormat.class, InvertedLinksMapper.class);

        job.setOutputFormatClass(TextOutputFormat.class);
        TextOutputFormat.setOutputPath(job, new Path(args[0], SolrPageDocDriver.DIR_NAME));

        job.setReducerClass(SolrPageDocReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(ObjectWritable.class);

        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);

        job.setJobName(jobName);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new SolrPageDocDriver(), args);
        System.exit(exitCode);
    }
}
