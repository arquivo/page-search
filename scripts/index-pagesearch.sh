#!/bin/bash
HADOOP_HOME=/opt/hadoop-3.2.1
HADOOP_EXEC=${HADOOP_HOME}/bin/hadoop
HADOOP_JAR_JOB=pagesearch-indexer-1.0.0-SNAPSHOT-jar-with-dependencies.jar

HADOOP_REDUCES_NUM=80

# get args from command line
INPUT_ARC_LOCATION=$1
OUTPUT_LOCATION=$2
COLLECTION=$3

LOCAL_INDEXING_DATA=/data/pagesearch_data/$COLLECTION

DATA_LOCATION="hdfs" # or remote

if [ $DATA_LOCATION == "hdfs" ]; then
  CREATE_PAGEDATA_JOB="pt.arquivo.indexer.mapreduce.HdfsPageSearchDataDriver"
else
  CREATE_PAGEDATA_JOB="pt.arquivo.indexer.mapreduce.PageSearchDataDriver"
fi

if [ ! -d $LOCAL_INDEXING_DATA ]; then
  mkdir -p $LOCAL_INDEXING_DATA
fi

function evaluate_and_exit() {
  if [ $? -ne 0 ]; then
    exit 1
  fi
}

## 1. Create PageData
echo "Create PageData from WARCS" >> "indexing_${COLLECTION}_timereport.txt"
\time --append -o "$LOCAL_INDEXING_DATA/indexing_${COLLECTION}_timereport.txt" -f "%E" $HADOOP_EXEC jar $HADOOP_JAR_JOB $CREATE_PAGEDATA_JOB -D jobName="CreatePageData_${COLLECTION}" -D collection=$COLLECTION $INPUT_ARC_LOCATION $OUTPUT_LOCATION
evaluate_and_exit

# 2. Invert Links
echo "Invert Links" >> "${LOCAL_INDEXING_DATA}/indexing_${COLLECTION}_timereport.txt"
\time --append -o "${LOCAL_INDEXING_DATA}/indexing_${COLLECTION}_timereport.txt" -f "%E" $HADOOP_EXEC jar $HADOOP_JAR_JOB pt.arquivo.indexer.mapreduce.InvertLinksDriver -D mapreduce.job.reduces=$HADOOP_REDUCES_NUM $OUTPUT_LOCATION
evaluate_and_exit

# 3. Merge and generate SolrDocs
echo "Merge and generate SolrDocs" >> "${LOCAL_INDEXING_DATA}/indexing_$COLLECTION_timereport.txt"
\time --append -o "${LOCAL_INDEXING_DATA}/indexing_${COLLECTION}_timereport.txt" -f "%E" $HADOOP_EXEC jar $HADOOP_JAR_JOB pt.arquivo.indexer.mapreduce.SolrPageDocDriver -D mapreduce.job.reduces=$HADOOP_REDUCES_NUM $OUTPUT_LOCATION
evaluate_and_exit

# 4. Copy to local filesystem
# MAKE SURE FOLDER EXIST
echo "Copy to Local Filesystem" >> "${LOCAL_INDEXING_DATA}/indexing_${COLLECTION}_timereport.txt"
$HADOOP_EXEC dfs -copyToLocal $OUTPUT_LOCATION/solr_data /data/$LOCAL_INDEXING_DATA/
