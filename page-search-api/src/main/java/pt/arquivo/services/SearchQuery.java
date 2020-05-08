package pt.arquivo.services;

public interface SearchQuery {
    String getQueryTerms();

    void setQueryTerms(String queryTerms);

    int getOffset();

    void setOffset(int offset);

    int getMaxItems();

    void setMaxItems(int maxItems);

    int getLimitPerSite();

    void setLimitPerSite(int limitPerSite);

    String[] getSite();

    void setSite(String[] site);

    String[] getType();

    void setType(String[] type);

    String[] getCollection();

    void setCollection(String[] collection);

    String getFrom();

    void setFrom(String from);

    String getTo();

    void setTo(String to);

    String[] getFields();

    void setFields(String[] fields);

    boolean isSearchBySite();

    boolean isSearchByType();

    boolean isSearchByCollection();
}
