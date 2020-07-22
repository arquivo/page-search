package pt.arquivo.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

import java.io.IOException;

@JsonSerialize(using = SearchResultSerializer.class)
public class SearchResultSolrImpl implements SearchResult {

    private static final Log LOG = LogFactory.getLog(SearchResultSolrImpl.class);

    private String title;
    private String originalURL;
    private String linkToArchive;
    private String tstamp;
    private long contentLength;
    private String digest;
    private String mimeType;
    private String encoding;
    private String date;
    private String linkToScreenshot;
    private String linkToNoFrame;
    private String linkToExtractedText;
    private String linkToMetadata;
    private String linkToOriginalFile;
    private String snippet;
    private String fileName;
    private String collection;
    private long offset;
    private Integer statusCode;
    private String id;

    private String[] fields;

    private SolrClient solrClient;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalURL() {
        return originalURL;
    }

    public void setOriginalURL(String originalURL) {
        this.originalURL = originalURL;
    }

    public String getLinkToArchive() {
        return linkToArchive;
    }

    public void setLinkToArchive(String linkToArchive) {
        this.linkToArchive = linkToArchive;
    }

    public String getTstamp() {
        return tstamp;
    }

    public void setTstamp(long tstamp) {
        this.tstamp = String.valueOf(tstamp);
    }

    public void setTimeStamp(String timeStamp) {
        this.tstamp = timeStamp;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLinkToScreenshot() {
        return linkToScreenshot;
    }

    public void setLinkToScreenshot(String linkToScreenshot) {
        this.linkToScreenshot = linkToScreenshot;
    }

    public String getLinkToNoFrame() {
        return linkToNoFrame;
    }

    public void setLinkToNoFrame(String linkToNoFrame) {
        this.linkToNoFrame = linkToNoFrame;
    }

    public String getLinkToExtractedText() {
        return linkToExtractedText;
    }

    public void setLinkToExtractedText(String linkToExtractedText) {
        this.linkToExtractedText = linkToExtractedText;
    }

    public String getLinkToMetadata() {
        return linkToMetadata;
    }

    public void setLinkToMetadata(String linkToMetadata) {
        this.linkToMetadata = linkToMetadata;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getLinkToOriginalFile() {
        return linkToOriginalFile;
    }

    public void setLinkToOriginalFile(String linkToOriginalFile) {
        this.linkToOriginalFile = linkToOriginalFile;
    }

    @JsonIgnore
    @Override
    public String getExtractedText() {
        StringBuilder extractedText = new StringBuilder();
        SolrQuery solQuery = new SolrQuery();
        solQuery.setQuery("id:".concat(this.id));
        try {
            QueryResponse queryResponse = solrClient.query(solQuery);
            SolrDocument doc = queryResponse.getResults().get(0);
            extractedText.append(doc.getFieldValue("title"));
            extractedText.append(", ");
            extractedText.append(doc.getFieldValue("content"));
        } catch (SolrServerException e) {
            // TODO log this
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return extractedText.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SolrClient getSolrClient() {
        return solrClient;
    }

    public void setSolrClient(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    public String getSearchResultId() {
        return getTstamp() + "/" + getOriginalURL();
    }
}


