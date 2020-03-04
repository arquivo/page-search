package org.arquivo.solr;

import org.apache.solr.common.SolrInputDocument;

import java.net.URL;
import java.util.Date;

// Has the Logic to build a SolrInputDocument
// Setting up each field information, normalizing or truncating if needed.
// TODO use Solrj Field annotation instead of this shit

public class SolrDocumentWrapper {
    private SolrInputDocument doc = new SolrInputDocument();

    public SolrDocumentWrapper(){
        this.doc = new SolrInputDocument();
    }

    public SolrDocumentWrapper(String warcName){
        // TODO REMOVE THIS
        // this.setWarcName(warcName);
    }

    public void setContentType(String contentType){
        // this doesnt make sense
        this.setType(contentType);

        String[] contentTypeParts = contentType.split("/");
        this.setPrimaryType(contentTypeParts[0]);

        // remove enconding hits passed on the content-type
        this.setSubType(contentTypeParts[1].split(";")[0]);
    }

    public void setId(String id){
        doc.setField(SolrFieds.ID, id);
    }

    public void setAnchor(String anchorContent) {
        doc.setField(SolrFieds.ANCHOR, anchorContent);
    }

    public void setWarcName(String warcName) {
        doc.setField(SolrFieds.WARCNAME, warcName);
    }

    public void setWarcOffset(String warcOffset) {
        doc.setField(SolrFieds.WARC_OFFSET, warcOffset);
    }

    public void setCollection(String collectionName) {
        doc.setField(SolrFieds.COLLECTION, collectionName);
    }

    public void setContent(String textContent) {
        doc.setField(SolrFieds.CONTENT, textContent);
    }

    public void setContentLength(int contentLength) {
        doc.setField(SolrFieds.CONTENT_LENGHT, contentLength);
    }

    public void setDate(String date) {
        doc.setField(SolrFieds.DATE, date);
    }

    public void setDigest(String digest) {
        doc.setField(SolrFieds.DIGEST, digest);
    }

    public void setDomain(String domain) {
        doc.setField(SolrFieds.DOMAIN, domain);
    }

    public void setEncoding(String encoding) {
        doc.setField(SolrFieds.ENCODING, encoding);
    }

    public void setHost(String host) {
        doc.setField(SolrFieds.HOST, host);
    }

    public void setInLinks(int inLinks) {
        doc.setField(SolrFieds.INLINKS, inLinks);
    }

    public void setOutLinks(int outLinks) {
        doc.setField(SolrFieds.OUTLINKS, outLinks);
    }

    public void setType(String type) {
        doc.setField(SolrFieds.TYPE, type);
    }

    public void setPrimaryType(String primaryType) {
        doc.setField(SolrFieds.PRIMARY_TYPE, primaryType);
    }

    public void setSubType(String subType) {
        doc.setField(SolrFieds.SUB_TYPE, subType);
    }

    public void setSite(String site) {
        doc.setField(SolrFieds.SITE, site);
    }

    public void setTimeStamp(String timeStamp) {
        doc.setField(SolrFieds.TSTAMP, timeStamp);
    }

    public void setUrl(String url) {
        doc.setField(SolrFieds.URL, url);
    }

    public SolrInputDocument getSolrInputDOcument(){
        return this.doc;
    }
}
