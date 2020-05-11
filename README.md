# Arquivo.pt Page Search System

This project aims to replace the legacy search system. The legacy search system, Nutchwax based, will be decrepated by Solr as the full-text search backend. In order to accomplish this, it provides a new API implementation to decouple Arquivo.pt API from the old project, making it backend agnostic and working with both Nutchwax and Solr systems.

## launching solr with pagesearch schema

docker build./solr -t pagesearch-solr 
docker run -d pagesearch-solr

## page-search-api

![](docs/img/PageSearchArchitecture.png)


## page-search-indexer

### warc-parser

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
```
Command example:
```
java -jar warc-parser-1.0.0-SNAPSHOT-jar-with-dependencies.jar /data/warcs
```

**Note**: The warc-parser doesn't extract inlinks/outlinks information.

### Indexing with Hadoop

Available jobs:
* HdfsPageSearchDataDriver
* PageSearchDataDriver
* InvertLinksDriver
* SolrPageDocDriver

Example of expected worklfow:
```
yarn jar pagesearch-indexer-1.0.0-SNAPSHOT.jar 
```

