package org.arquivo.api;

import com.google.gson.annotations.SerializedName;

public class SearchResult {

    private String serviceName;
    private String linkToService;
    private String nextPage;
    private String previousPage;
    private long totalItems;
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
    private long offset;

    private TextSearchRequestParameters requestParameter;

    @SerializedName( "estimated_nr_results" )
    private String estimatedNumberResults;

    @SerializedName( "request_parameters" )
    private TextSearchRequestParameters requestParameters; //request input parameters

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getLinkToService() {
        return linkToService;
    }

    public void setLinkToService(String linkToService) {
        this.linkToService = linkToService;
    }

    public String getNextPage() {
        return nextPage;
    }

    public void setNextPage(String nextPage) {
        this.nextPage = nextPage;
    }

    public String getPreviousPage() {
        return previousPage;
    }

    public void setPreviousPage(String previousPage) {
        this.previousPage = previousPage;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

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

    public String getEstimatedNumberResults() {
        return estimatedNumberResults;
    }

    public void setEstimatedNumberResults(String estimatedNumberResults) {
        this.estimatedNumberResults = estimatedNumberResults;
    }

    public TextSearchRequestParameters getRequestParameters() {
        return requestParameters;
    }

    public void setRequestParameters(TextSearchRequestParameters requestParameters) {
        this.requestParameters = requestParameters;
    }

    public TextSearchRequestParameters getRequestParameter() {
        return requestParameter;
    }

    public void setRequestParameter(TextSearchRequestParameters requestParameter) {
        this.requestParameter = requestParameter;
    }

    //@SerializedName( "response_items" )
    //private List<Item> itens;
}


