package pt.arquivo.indexer.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import pt.arquivo.indexer.data.ArchiveFileInputFormat;
import pt.arquivo.indexer.data.PageData;
import pt.arquivo.indexer.data.WebArchiveKey;


public class HdfsPageSearchDataDriver extends Configured implements Tool {

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

        // TODO change ArchiveFileInputFormat to verify if file ends with arc or warc.gz
        job.setInputFormatClass(ArchiveFileInputFormat.class);
        ArchiveFileInputFormat.setInputPaths(job, new Path(args[0]));
        ArchiveFileInputFormat.setInputDirRecursive(job, true);

        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        SequenceFileOutputFormat.setOutputPath(job, new Path(args[1], HdfsPageSearchDataDriver.DIR_NAME));

        // map only job
        job.setNumReduceTasks(0);
        job.setMapperClass(HdfsPageSearchDataMapper.class);

        job.setMapOutputKeyClass(WebArchiveKey.class);
        job.setMapOutputValueClass(PageData.class);

        job.setOutputKeyClass(WebArchiveKey.class);
        job.setOutputValueClass(PageData.class);

        job.setJobName(jobName);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new HdfsPageSearchDataDriver(), args);
        System.exit(exitCode);
    }
}
