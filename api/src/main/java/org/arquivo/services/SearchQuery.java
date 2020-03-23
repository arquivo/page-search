package org.arquivo.services;

public interface SearchQuery {
    String getQueryTerms();
    void setQueryTerms(String queryTerms);

    public int getOffset();
    public void setOffset(int offset);

    int getLimit();
    void setLimit(int limit);

    int getLimitPerSite();
    void setLimitPerSite(int limitPerSite);

    String[] getSite();
    void setSite(String[] site);

    String getType();
    void setType(String type);

    String getCollection();
    void setCollection(String collection);

    String getFrom();
    void setFrom(String from);

    String getTo();
    void setTo(String to);
}
