package pt.arquivo.services;

public interface SearchResult {
    String getExtractedText();
    String getSearchResultId();
    String getTitle();
    String getCollection();
    String getEncoding();
    String getId();
    String[] getFields();
    Integer getStatusCode();
    String getTstamp();
    String getDigest();

    void setStatusCode(Integer statusCode);
    void setCollection(String collection);
    void setOffset(long offset);
    void setFileName(String fileName);
    void setContentLength(long contentLength);
    void setDigest(String digest);
}

