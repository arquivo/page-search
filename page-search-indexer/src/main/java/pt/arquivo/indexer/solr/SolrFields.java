package pt.arquivo.indexer.solr;

public interface SolrFields {
    String ID = "id";
    String TITLE = "title";
    String ANCHOR = "anchor";
    String COLLECTION = "collection";

    String CONTENT = "content";
    String CONTENT_LENGHT = "contentLength";
    String DIGEST = "digest";

    String TSTAMP = "tstamp";

    // FIXME WHAT IS THE DIFERENCE BETWEEN THIS AND DOMAIN?
    String DOMAIN = "domain";
    String HOST = "host";
    String SITE = "site";
    String URL = "url";

    String INLINKS = "inlinks";
    String OUTLINKS = "outlinks";

    String TYPE = "type";
    String PRIMARY_TYPE = "primaryType";
    String SUB_TYPE = "subType";
    String TIKA_TYPE = "tika_content_type";
    String ENCODING = "encoding";

    String WARCNAME = "warc_name";
    String WARC_OFFSET = "warc_offset";
}
