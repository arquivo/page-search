package org.arquivo.indexer.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.arquivo.indexer.data.ArchiveFileInputFormat;
import org.arquivo.indexer.data.PageSearchData;


public class HdfsPageSearchIndexDriver extends Configured implements Tool {

    @Override
    public int run(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.printf("Usage: %s [generic options] <collection> <input> <output>\n", getClass().getSimpleName());
            ToolRunner.printGenericCommandUsage(System.err);
            return -1;
        }

        Configuration conf = getConf();
        String jobName = conf.get("jobName", "teste");

        Job job = Job.getInstance(conf);
        job.setJarByClass(getClass());

        job.setInputFormatClass(ArchiveFileInputFormat.class);
        ArchiveFileInputFormat.addInputPath(job, new Path(args[0]));

        job.setMapperClass(HdfsPageSearchDataMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(PageSearchData.class);

        // set reduces
        job.setReducerClass(SolrDocumentReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        //set outputFormat
        job.setOutputFormatClass(TextOutputFormat.class);
        TextOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setJobName(jobName);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new HdfsPageSearchIndexDriver(), args);
        System.exit(exitCode);
    }
}
