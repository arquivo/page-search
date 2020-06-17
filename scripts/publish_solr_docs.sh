#!/bin/bash

HADOOP_HOME=/opt/hadoop-3.1.2/
HADOOP_EXEC=$HADOOP_HOME/bin/hadoop
HADOOP_JAR_JOB=pagesearch-indexer-1.0.0-SNAPSHOT-jar-with-dependencies.jar

# get args from command line
INPUT_ARC_LOCATION=$1
OUTPUT_LOCATION=$2

# 1. Create PageData
$HADOOP_EXEC jar $HADOOP_JAR_JOB pt.arquivo.indexer.mapreduce.HdfsPageSearchDataDriver -D collection=$COLLECTION $INPUT_ARC_LOCATION $OUTPUT_LOCATION

# 2. Invert Links
$HADOOP_EXEC jar $HADOOP_JAR_JOB pt.arquivo.indexer.mapreduce.InvertLinksDriver -D mapreduce.job.reduces=22 $OUTPUT_LOCATION

# 3. Merge and generate SolrDocs
$HADOOP_EXEC jar $HADOOP_JAR_JOB pt.arquivo.indexer.mapreduce.SolrPageDocDriver -D mapreduce.job.reduces=22 $OUTPUT_LOCATION

# 4. Copy to local filesystem
