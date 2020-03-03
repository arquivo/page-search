package org.arquivo.solr;

public interface SolrFieds {
    public static final String ID = "id";
    public static final String ANCHOR = "anchor";
    // or arc?
    public static final String COLLECTION = "collection";

    public static final String CONTENT = "content";
    public static final String CONTENT_LENGHT = "content_lenght";
    public static final String DIGEST = "digest";

    public static final String DATE = "date";
    public static final String TSTAMP = "tsamp";

    // WHAT IS THE DIFERENCE BETWEEN THIS AND DOMAIN?
    public static final String DOMAIN = "domain";
    public static final String HOST = "host";
    public static final String SITE = "site";
    public static final String URL = "url";

    public static final String INLINKS = "inlinks";
    public static final String OUTLINKS = "outlinks";

    public static final String TYPE = "type";
    public static final String PRIMARY_TYPE = "primary_type";
    public static final String SUB_TYPE = "sub_type";
    public static final String ENCODING = "encoding";

    public static final String WARCNAME = "warc_name";
    public static final String WARC_OFFSET = "warc_offset";
}
