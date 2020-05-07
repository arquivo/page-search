# Arquivo.pt Page Search System

This project aims to replace the legacy search system. The legacy search system, Nutchwax based, will be decrepated by Solr as the full-text search backend. In order to accomplish this, it provides a new API implementation to decouple Arquivo.pt API from the old project, making it backend agnostic and working with both Nutchwax and Solr systems.

## launching solr with pagesearch schema

docker build./solr -t pagesearch-solr 
docker run -d pagesearch-solr

## page-search-api

![](docs/img/PageSearchArchitecture.png)


## page-search-indexer

### warc-parser (do not generate inlinks/outlinks)

warc-parser-1.0.0-SNAPSHOT-jar-with-dependencies.jar

### indexing with hadoop

