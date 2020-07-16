## Page Search API

* Add option to display indexes ID's 
* Display the dedupField which the results are being deduplicated and dedup by URL when is a Site Search query.
* 'Callback' option decreprated
* Pagination properly works:
    * **previous_page** is not displayed if we are at the first page.
    * **next_page** is just displayed if there are indeed more results.
    
* Fields types are properly represented. If it is a number is displayed as that instead of everything being a String.

## Page Search Backend
* Support for Nutchwax Backend
* Support for Solr Backend

## Page Search Indexer
* (W)ARC Parser
* Distributed Indexing of (W)ARCS to be Ingested by Solr Backend.
* Hadoop ArchiveRecordInputFormat - Allows to index WARCS directly from HDFS.

