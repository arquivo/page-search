# Arquivo.pt Page Search System

This project aims to replace the legacy search system. The legacy search system, Nutchwax based, will be decrepated by Solr as the full-text search backend. In order to accomplish this, it provides a new API implementation to decouple Arquivo.pt API from the old project, making it backend agnostic and working with both Nutchwax and Solr systems.

## Compile Page Search

To be able to compile the project we need in the machine's maven repository the Arquivo.pt Nutchwax Project libraries.
In order for Page Search API to use NutchWaxSearchService as the full-text backend (In the future we can remove these libraries).

1. Satisfy the following page-search-api/pom.xml requirements for NutchWaxSearchService (legacy backend):
```
<dependency>
    <groupId>pt.arquivo</groupId>
    <artifactId>pwalucene</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<dependency>
    <groupId>org.archive.nutchwax</groupId>
    <artifactId>nutchwax-plugins</artifactId>
    <version>0.11.0-SNAPSHOT</version>
</dependency>
```

You will need to compile pwa-technologies to satisfy this:

```shell script
$ git clone https://github.com/arquivo/pwa-technologies.git
$ mvn clean install -f pwa-technologies/PwaLucene/pom.xml
$ mvn clean install -f pwa-technologies/PwaArchive-access/pom.xml
```

2. Clone and compile Page Search

```
$ git clone https://github.com/arquivo/pagesearch.git
$ mvn clean install -f pagesearch/pom.xml
```

### Run without integration Tests

To do this just run mvn install with the following profile deactivated:
```
$ mvn clean install -f pagesearch/pom.xml -P !docker-integration-tests
```

### Generate new arquivo/pagesearch-solr-test docker image for the integration tests

Note: you need log with hub.docker.com to be able to publish an image to Arquivo's docker hub repository.

```
$ cd pagesearch/scripts
$ ./build-solr-test-image.sh
```

## Page Search API Architecture 

![](docs/img/PageSearchArchitecture.png)

### Api Documentation

https://preprod.arquivo.pt/pagesearch/swagger-ui.html#/

## Page Search Indexer

### How to index a collection of WARC files 

Available jobs:
* HdfsPageSearchDataDriver
* PageSearchDataDriver
* InvertLinksDriver
* SolrPageDocDriver

Example of expected workflow:
```shell script
$ yarn jar pagesearch-index-job-0.0.1-jar-with-dependencies.jar HdfsPageSearchDataDriver -D collection="TESTE" input.txt output
$ yarn jar pagesearch-index-job-0.0.1-jar-with-dependencies.jar InvertLinksDriver -D mapred.reduce.tasks=<nr_reduces> output
$ yarn jar pagesearch-index-job-0.0.1-jar-with-dependencies.jar SolrPageDocDriver -D mapred.reduce.tasks=<nr_reduces> output
```

### Full Indexing Workflow script:

[https://github.com/arquivo/page-search/blob/master/scripts/index-pagesearch.sh](https://github.com/arquivo/page-search/blob/master/scripts/index-pagesearch.sh)

```shell script
$ ./index-pagesearch.sh <hdfs_warcfiles_folder> <hdfs_output_folder> <collection_name>
```
Example:
```shell script
$ ./index-pagesearch.sh /user/dbicho/AWP2 /user/dbicho/output_AWP2 AWP2
```


### Configuring which records to process and index

Write a reference.conf with the parsing configurations. The default configurations of the parser are:
```
{
    "warc" : {
        "solr":{
            "server": "http://localhost:8983/solr/searchpages"
        },
        "index":{
            "extract":{
                # Restrict record types:
                "record_type_include" : [
                    response, revisit
                ],
                "record_response_include" : [
                    "2"
                ],
                "record_primary_mimetype_include" : [
                    text, application
                ],
                "record_mimetype_exclude" : [
                    xml, css, javascript, x-javascript, json
                ]
            }
        }
    }
}


