package pt.arquivo.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.StringUtils;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchQueryImpl implements SearchQuery {

    private static final int MAX_ALLOWED_ITEMS = 2000; 

    @JsonProperty("q")
    private String queryTerms;

    private int offset = 0;

    @JsonProperty("maxItems")
    private int maxItems = 50;

    // TODO decreprated field
    @JsonIgnore
    private Integer limitPerSite;

    private int dedupValue = 2;

    private String from;
    private String to;
    private String[] type;

    @JsonProperty("siteSearch")
    private String[] site;

    private String[] collection;

    private String[] fields;

    @JsonIgnore
    private boolean prettyPrint;

    private String dedupField = "site";

    public SearchQueryImpl(String queryTerms) {
        this.queryTerms = queryTerms;
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

    public int getMaxItems() {
        return maxItems;
    }

    public void setMaxItems(int maxItems) {
        if (maxItems < 0) {
            this.maxItems = 0;
        } else if (maxItems > MAX_ALLOWED_ITEMS){
            this.maxItems = MAX_ALLOWED_ITEMS;
        } else {
            this.maxItems = maxItems;
        }
    }

    public Integer getLimitPerSite() {
        return limitPerSite;
    }

    public void setLimitPerSite(Integer limitPerSite) {
        this.limitPerSite = limitPerSite;
        this.dedupValue = limitPerSite;
    }

    public String getFrom() {
        return from;
    }

    // TODO validate if its valid date (just numbers and at least a year)
    public void setFrom(String from) {
        if (from.length() > 14) {
            this.from = from.substring(0, 14);
        } else {
            this.from = StringUtils.rightPad(from, 14, "0");
        }
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        if (to.length() > 14) {
            this.to = to.substring(0, 14);
        } else {
            this.to = StringUtils.rightPad(to, 14, "0");
        }
    }

    public String[] getType() {
        return type;
    }

    public void setType(String[] type) {
        this.type = type;
    }

    public String[] getSite() {
        return site;
    }

    public void setSite(String[] site) {
        this.site = site;
    }

    public String[] getCollection() {
        return collection;
    }

    public void setCollection(String[] collection) {
        this.collection = collection;
    }

    public boolean getPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }


    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    @Override
    public String getDedupField() {
        return dedupField;
    }

    @Override
    public void setDedupField(String dedupField) {
        this.dedupField = dedupField;
    }

    public int getDedupValue() {
        return dedupValue;
    }

    public void setDedupValue(int dedupValue) {
        this.dedupValue = dedupValue;
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    @Override
    @JsonIgnore
    public boolean isSearchBySite() {
        return this.site != null && this.site.length > 0;
    }

    @Override
    @JsonIgnore
    public boolean isSearchByType() {
        return this.type != null && this.type.length > 0;
    }

    @Override
    @JsonIgnore
    public boolean isSearchByCollection() {
        return this.collection != null && this.collection.length > 0;
    }

    @Override
    @JsonIgnore
    public boolean isTimeBoundedQuery() {
        return (this.getTo() != null || this.getFrom() != null);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("q: ").append(this.queryTerms);
        stringBuilder.append(" offset: ").append(this.offset);
        stringBuilder.append(" maxItems: ").append(this.maxItems);
        stringBuilder.append(" itemsPerSite: ").append(this.limitPerSite);
        stringBuilder.append(" from: ").append(this.from);
        stringBuilder.append(" to: ").append(this.to);
        stringBuilder.append(" type: ").append(this.type);
        if (this.site != null) {
            for (int i = 0; i < this.site.length; i++) {
                stringBuilder.append(" site: ").append(this.site[i]);
            }
        } else {
            stringBuilder.append(" site: ").append(this.site);
        }
        stringBuilder.append(" collection: ").append(this.collection);
        stringBuilder.append(" prettyPrint: ").append(this.prettyPrint);
        return stringBuilder.toString();
    }

}