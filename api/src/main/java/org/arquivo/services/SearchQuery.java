package org.arquivo.services;

public interface SearchQuery {
    String getQueryTerms();
    void setQueryTerms(String queryTerms);

    String getLimit();
    void setLimit(String limit);

    String getLimitPerSite();
    void setLimitPerSite(String limitPerSite);

    String getSite();
    void setSite(String site);

    String getType();
    void setType(String type);

    String getCollection();
    void setCollection(String collection);

    String getFrom();
    void setFrom(String from);

    String getTo();
    void setTo(String to);
}
