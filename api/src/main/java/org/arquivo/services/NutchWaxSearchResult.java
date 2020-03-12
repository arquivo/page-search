package org.arquivo.services;

import org.apache.nutch.searcher.HitDetails;
import org.apache.nutch.searcher.NutchBean;
import org.springframework.context.annotation.Lazy;

import java.beans.Transient;
import java.io.IOException;

public class NutchWaxSearchResult implements SearchResult {

    private String title;
    private String originalURL;
    private String linkToArchive;
    private long timeStamp;
    private long contentLength;
    private String digest;
    private String mimeType;
    private String encoding;
    private String date;
    private String linkToScreenshot;
    private String linkToNoFrame;
    private String linkToExtractedText;
    private String linkToMetadata;
    private String snippet;
    private String statusCode;
    private String fileName;
    private String collection;
    private long offset;

    private NutchBean bean;
    private HitDetails details;

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

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
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

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
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

    @Transient
    @Override
    public String getExtractedText() {
        try {
            return this.bean.getParseText(this.details).getText();
        } catch (IOException e) {
            // TODO log this
            e.printStackTrace();
        }
        return "";
    }

    @Transient
    public HitDetails getDetails() {
        return details;
    }

    public void setDetails(HitDetails details) {
        this.details = details;
    }

    @Transient
    public NutchBean getBean() {
        return bean;
    }

    public void setBean(NutchBean bean) {
        this.bean = bean;
    }
}


