package org.arquivo.indexer.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.arquivo.indexer.data.PageData;
import org.arquivo.indexer.data.WebArchiveKey;


public class PageSearchDataDriver extends Configured implements Tool {

    public static final String DIR_NAME = "page_data";

    @Override
    public int run(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.printf("Usage: %s [generic options] <collection> <input> <output>\n", getClass().getSimpleName());
            ToolRunner.printGenericCommandUsage(System.err);
            return -1;
        }

        Configuration conf = getConf();
        String jobName = conf.get("jobName", "CreatePageData");

        Job job = Job.getInstance(conf);
        job.setJarByClass(getClass());

        job.setInputFormatClass(NLineInputFormat.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);

        // Re-enable this
        // SequenceFileOutputFormat.setCompressOutput(job, true);

        NLineInputFormat.addInputPath(job, new Path(args[0]));
        SequenceFileOutputFormat.setOutputPath(job, new Path(args[1], PageSearchDataDriver.DIR_NAME));

        job.setMapperClass(PageSearchDataMapper.class);
        // map-only job

        job.setNumReduceTasks(0);
        // use an identity reducer so we have the same partition as inverted links data, and we can do a map-side join later
        // job.setReducerClass(PageSearchDataReducer.class);

        // probably need to change this key right?
        job.setMapOutputKeyClass(WebArchiveKey.class);
        job.setMapOutputValueClass(PageData.class);

        job.setOutputKeyClass(WebArchiveKey.class);
        job.setOutputValueClass(PageData.class);

        job.setJobName(jobName);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new PageSearchDataDriver(), args);
        System.exit(exitCode);
    }
}
