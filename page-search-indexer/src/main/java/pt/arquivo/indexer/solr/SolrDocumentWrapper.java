package pt.arquivo.indexer.solr;

import org.apache.solr.common.SolrInputDocument;

// Has the Logic to build a SolrInputDocument
// Setting up each field information, normalizing or truncating if needed.
public class SolrDocumentWrapper {
    private SolrInputDocument doc;

    public SolrDocumentWrapper(){
        this.doc = new SolrInputDocument();
    }

    public SolrDocumentWrapper(String warcName){
        this.doc = new SolrInputDocument();
        this.setWarcName(warcName);
    }

    public void setContentType(String contentType){
        String type = contentType.split(";")[0];
        this.setType(type);

        String[] contentTypeParts = type.split("/");
        this.setPrimaryType(contentTypeParts[0]);
        this.setSubType(contentTypeParts[1]);
    }

    public void setId(String id){
        doc.setField(SolrFields.ID, id);
    }

    public void setAnchor(String anchorContent) {
        doc.setField(SolrFields.ANCHOR, anchorContent);
    }

    public void setWarcName(String warcName) {
        doc.setField(SolrFields.WARCNAME, warcName);
    }

    public void setWarcOffset(String warcOffset) {
        doc.setField(SolrFields.WARC_OFFSET, warcOffset);
    }

    public void setCollection(String collectionName) {
        doc.setField(SolrFields.COLLECTION, collectionName);
    }

    public void setContent(String textContent) {
        doc.setField(SolrFields.CONTENT, textContent);
    }

    public void setContentLength(int contentLength) {
        doc.setField(SolrFields.CONTENT_LENGHT, contentLength);
    }


    public void setDigest(String digest) {
        doc.setField(SolrFields.DIGEST, digest);
    }

    public void setDomain(String domain) {
        doc.setField(SolrFields.DOMAIN, domain);
    }

    public void setEncoding(String encoding) {
        doc.setField(SolrFields.ENCODING, encoding);
    }

    public void setTitle(String title) { doc.setField(SolrFields.TITLE, title);}

    public void setHost(String host) {
        doc.setField(SolrFields.HOST, host);
    }

    public void setInLinks(int inLinks) {
        doc.setField(SolrFields.INLINKS, inLinks);
    }

    public void setOutLinks(int outLinks) {
        doc.setField(SolrFields.OUTLINKS, outLinks);
    }

    public void setTikaType(String tikaType) { doc.setField(SolrFields.TIKA_TYPE, tikaType);}

    public void setType(String type) {
        doc.setField(SolrFields.TYPE, type);
    }

    public void setPrimaryType(String primaryType) {
        doc.setField(SolrFields.PRIMARY_TYPE, primaryType);
    }

    public void setSubType(String subType) {
        doc.setField(SolrFields.SUB_TYPE, subType);
    }

    public void setSite(String site) {
        doc.setField(SolrFields.SITE, site);
    }

    public void setTimeStamp(String timeStamp) {
        doc.setField(SolrFields.TSTAMP, timeStamp);
    }

    public void setUrl(String url) {
        doc.setField(SolrFields.URL, url);
    }

    public SolrInputDocument getSolrInputDocument(){
        return this.doc;
    }
}
