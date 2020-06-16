package pt.arquivo.indexer.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import pt.arquivo.indexer.data.Inlink;
import pt.arquivo.indexer.data.Inlinks;
import pt.arquivo.indexer.data.WebArchiveKey;

public class InvertLinksDriver extends Configured implements Tool {

    // Specify Time Dimension through configuration
    // This can be Yearly, Monthly, Daily
    @Override
    public int run(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.printf("Usage: %s [generic options] <input>\n", getClass().getSimpleName());
            ToolRunner.printGenericCommandUsage(System.err);
            return -1;
        }
        Configuration conf = getConf();
        String jobName = conf.get("jobName", "InvertLinks");

        Job job = Job.getInstance(conf);
        job.setJarByClass(getClass());

        job.setInputFormatClass(SequenceFileInputFormat.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);

        SequenceFileInputFormat.addInputPath(job, new Path(args[0], PageSearchDataDriver.DIR_NAME));
        SequenceFileOutputFormat.setOutputPath(job, new Path(args[0], Inlinks.DIR_NAME));

        job.setMapperClass(InvertLinksMapper.class);
        job.setReducerClass(InvertLinksReducer.class);

        job.setMapOutputKeyClass(WebArchiveKey.class);
        job.setMapOutputValueClass(Inlink.class);

        job.setOutputKeyClass(WebArchiveKey.class);
        job.setOutputValueClass(Inlinks.class);

        job.setJobName(jobName);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new InvertLinksDriver(), args);
        System.exit(exitCode);
    }
}
