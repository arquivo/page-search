package org.arquivo.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.annotations.SerializedName;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchQueryImpl implements SearchQuery {

    @SerializedName("q")
    private String queryTerms;

    @SerializedName("offset")
    private int offset = 0;

    @SerializedName("maxItems")
    private int maxItems = 50;

    @SerializedName("itemsPerSite")
    private int limitPerSite = 2;

    private String from;
    private String to;
    private String type;

    @SerializedName("siteSearch")
    private String[] site;

    @SerializedName("collection")
    private String collection;

    private String[] fields;
    private String prettyPrint;

    public SearchQueryImpl(String queryTerms) {
        this.queryTerms = queryTerms;
    }

    public SearchQueryImpl(String queryTerms, int offset, int maxItems, int limitPerSite,
                           String from, String to, String type, String[] site,
                           String collection, String[] fields, String prettyPrint) {

        this.queryTerms = queryTerms;
        this.offset = offset;
        this.maxItems = maxItems;
        this.limitPerSite = limitPerSite;
        this.from = from;
        this.to = to;
        this.type = type;
        this.site = site;
        this.collection = collection;
        this.prettyPrint = prettyPrint;

        this.setFields(fields);
    }

    public String getQueryTerms() {
        return queryTerms;
    }

    public void setQueryTerms(String queryTerms) {
        this.queryTerms = queryTerms;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return maxItems;
    }

    @Override
    public void setLimit(int maxItems) {
        this.maxItems = maxItems;

    }

    public int getLimitPerSite() {
        return limitPerSite;
    }

    public void setLimitPerSite(int limitPerSite) {
        this.limitPerSite = limitPerSite;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from.substring(0, 14);
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getSite() {
        return site;
    }

    public void setSite(String[] site) {
        this.site = site;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(String prettyPrint) {
        this.prettyPrint = prettyPrint;
    }


    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

//    @Override
//    public String toString() {
//        StringBuilder strFieldsBuilder = new StringBuilder();
//        if (fields != null) {
//            for (String field : fields) {
//                strFieldsBuilder.append(field);
//                strFieldsBuilder.append(",");
//            }
//        }
//
//        StringBuilder stringBuilderSite = new StringBuilder();
//        for (int i = 0; i < site.length; i++) {
//                stringBuilderSite.append(site[i]);
//                if (i != site.length - 1){
//                    stringBuilderSite.append(",");
//                }
//        }
//
//        String strFields = strFieldsBuilder.toString().substring(0, strFieldsBuilder.length() - 1);
//        return "TextSearchRequestParameters [queryTerms=" + queryTerms + ", offset=" + offset + ", maxitems=" + maxItems
//                + ", limitPerSite=" + limitPerSite + ", from=" + from + ", to=" + to + ", type="
//                + type + ", site=" + stringBuilderSite.toString() + ", collection=" + collection + ", fields=" + strFields + ", prettyPrint=" + prettyPrint + "]";
//    }
}