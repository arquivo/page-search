package pt.arquivo.services;

public interface SearchResultI {
    public void setTitle(String title);

    public String getOriginalURL();

    public void setOriginalURL(String originalURL);

    public String getLinkToArchive();

    public void setLinkToArchive(String linkToArchive);

    public String getTstamp();

    public void setTstamp(Long tstamp);

    public void setTimeStamp(String timeStamp);

    public Long getContentLength();

    public void setContentLength(long contentLength);

    public String getDigest();

    public void setDigest(String digest);

    public String getMimeType();

    public void setMimeType(String mimeType);

    public String getEncoding();

    public void setEncoding(String encoding);

    public String getDate();

    public void setDate(String date);

    public String getLinkToScreenshot();

    public void setLinkToScreenshot(String linkToScreenshot);

    public String getLinkToNoFrame();

    public void setLinkToNoFrame(String linkToNoFrame);

    public String getLinkToExtractedText();

    public void setLinkToExtractedText(String linkToExtractedText);

    public String getLinkToMetadata();

    public void setLinkToMetadata(String linkToMetadata);

    public String getSnippet();

    public void setSnippet(String snippet);

    public String getFileName();

    public void setFileName(String fileName);

    public Long getOffset();

    public void setOffset(long offset);

    public String getCollection();

    public void setCollection(String collection);

    public Integer getStatusCode();

    public void setStatusCode(Integer statusCode);

    public String getLinkToOriginalFile();

    public void setLinkToOriginalFile(String linkToOriginalFile);

    public String[] getFields();

    public void setFields(String[] fields);

    public String getId();

    public void setId(String id);

    public String getSearchResultId();

    public String getExtractedText();

}
