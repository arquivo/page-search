package org.arquivo.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.annotations.SerializedName;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NutchWaxSearchQuery implements SearchQuery {

    @SerializedName("q")
    private String queryTerms;

    @SerializedName("offset")
    private String offset = "0";

    @SerializedName("maxItems")
    private String maxItems = "50";

    @SerializedName("itemsPerSite")
    private String limitPerSite = "2";

    private String from;
    private String to;
    private String type;

    @SerializedName("siteSearch")
    private String site;

    @SerializedName("collection")
    private String collection;

    private String[] fields;
    private String prettyPrint;

    public NutchWaxSearchQuery(String queryTerms) {
        this.queryTerms = queryTerms;
    }

    public NutchWaxSearchQuery(String queryTerms, String offset, String maxItems, String limitPerSite,
                               String from, String to, String type, String site,
                               String collection, String fields[], String prettyPrint) {

        this.queryTerms = queryTerms;
        this.offset = offset;
        this.maxItems = maxItems;
        this.limitPerSite = limitPerSite;
        this.from = from;
        this.to = to;
        this.type = type;
        this.site = site;
        this.collection = collection;
        this.setFields(fields);
        this.prettyPrint = prettyPrint;
    }


    public String getQueryTerms() {
        return queryTerms;
    }

    public void setQueryTerms(String queryTerms) {
        this.queryTerms = queryTerms;
    }

    public String getStart() {
        return offset;
    }

    public void setStart(String offset) {
        this.offset = offset;
    }

    public String getLimit() {
        return maxItems;
    }

    public void setLimit(String maxItems) {
        this.maxItems = maxItems;
    }

    public String getLimitPerSite() {
        return limitPerSite;
    }

    public void setLimitPerSite(String limitPerSite) {
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

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
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

    @Override
    public String toString() {
        StringBuilder strFieldsBuilder = new StringBuilder();
        if (fields != null) {
            for (String field : fields) {
                strFieldsBuilder.append(field);
                strFieldsBuilder.append(",");
            }
        }
        String strFields = strFieldsBuilder.toString().substring(0, strFieldsBuilder.length() - 1);
        return "TextSearchRequestParameters [queryTerms=" + queryTerms + ", offset=" + offset + ", maxitems=" + maxItems
                + ", limitPerSite=" + limitPerSite + ", from=" + from + ", to=" + to + ", type="
                + type + ", site=" + site + ", collection=" + collection + ", fields=" + strFields + ", prettyPrint=" + prettyPrint + "]";
    }
}