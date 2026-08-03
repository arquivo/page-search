package pt.arquivo.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchQueryImpl implements SearchQuery {

    private static final int MAX_ALLOWED_ITEMS = 500;

    private static final List<String> MIN_LANGUAGE_CONFIDENCE_VALUES =
            Arrays.asList(LANGUAGE_CONFIDENCE_HIGH, LANGUAGE_CONFIDENCE_MEDIUM, LANGUAGE_CONFIDENCE_LOW);

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

    @JsonProperty("titleSearch")
    private String titleSearch;

    private String language;

    private String minLanguageConfidence;

    public SearchQueryImpl(String queryTerms) {
        this.queryTerms = queryTerms;
    }

    public String getQueryTerms() {
        return queryTerms;
    }

    @JsonIgnore
    public String getQuotedQueryTerms() {
        if (queryTerms == null) {
            return null;
        }
        // if the query is already quoted, don't quote it again
        if (queryTerms.startsWith("\"") && queryTerms.endsWith("\"")) {
            return queryTerms;
        }
        if (queryTerms.startsWith("'") && queryTerms.endsWith("'")) {
            return queryTerms;
        }
        return "\"" + queryTerms + "\"";
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

    public String getTitleSearch(){
        return titleSearch;
    }

    public void setTitleSearch(String titleSearch){
        this.titleSearch = titleSearch;
    }

    @Override
    @JsonIgnore
    public boolean isSearchByTitle() {
        return this.titleSearch != null;
    }

    public String getLanguage() {
        return language;
    }

    /** Language codes are indexed in lowercase, e.g. pt */
    public void setLanguage(String language) {
        if (language == null || language.trim().isEmpty()) {
            this.language = null;
        } else {
            this.language = language.trim().toLowerCase();
        }
    }

    @Override
    @JsonIgnore
    public boolean isSearchByLanguage() {
        return this.language != null;
    }

    /**
     * The lowest languageConfidence tier the results may have. Defaults to HIGH when filtering by language, and to
     * no confidence filtering at all when the query doesn't ask about the language.
     *
     * @return HIGH, MEDIUM, LOW, or null when the results should not be filtered by confidence
     */
    public String getMinLanguageConfidence() {
        if (minLanguageConfidence == null && isSearchByLanguage()) {
            return LANGUAGE_CONFIDENCE_HIGH;
        }
        return minLanguageConfidence;
    }

    /**
     * @param minLanguageConfidence HIGH, MEDIUM or LOW (case insensitive), or null to leave it at the default
     * @throws IllegalArgumentException when the tier isn't one that can be asked for
     */
    public void setMinLanguageConfidence(String minLanguageConfidence) {
        if (minLanguageConfidence == null || minLanguageConfidence.trim().isEmpty()) {
            this.minLanguageConfidence = null;
            return;
        }
        String tier = minLanguageConfidence.trim().toUpperCase();
        if (!MIN_LANGUAGE_CONFIDENCE_VALUES.contains(tier)) {
            throw new IllegalArgumentException("Invalid minLanguageConfidence: " + minLanguageConfidence
                    + ". Valid values are " + String.join(", ", MIN_LANGUAGE_CONFIDENCE_VALUES) + ".");
        }
        this.minLanguageConfidence = tier;
    }

    /**
     * The query is spellchecked when the user asks for the spellcheck field, e.g. fields=title,spellcheck
     */
    @Override
    @JsonIgnore
    public boolean isSpellcheck() {
        if (this.fields == null) {
            return false;
        }
        for (String field : this.fields) {
            if (SPELLCHECK_FIELD.equalsIgnoreCase(field)) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("q: ").append(getQueryTerms());
        stringBuilder.append(" offset: ").append(getOffset());
        stringBuilder.append(" maxItems: ").append(getMaxItems());
        stringBuilder.append(" itemsPerSite: ").append(getLimitPerSite());
        stringBuilder.append(" from: ").append(getFrom());
        stringBuilder.append(" to: ").append(getTo());
        stringBuilder.append(" type: ").append(getType());
        if (isSearchBySite()) {
            for (int i = 0; i < this.site.length; i++) {
                stringBuilder.append(" siteSearch: ").append(this.site[i]);
            }
        } else {
            stringBuilder.append(" siteSearch: ").append(getSite());
        }
        if (isSearchByCollection()) {
            for (int i = 0; i < this.collection.length; i++) {
                stringBuilder.append(" collection: ").append(this.collection[i]);
            }
        } else {
            stringBuilder.append(" collection: ").append(getCollection());
        }
        stringBuilder.append(" titleSearch: ").append(getTitleSearch());
        stringBuilder.append(" language: ").append(getLanguage());
        stringBuilder.append(" minLanguageConfidence: ").append(getMinLanguageConfidence());
        stringBuilder.append(" prettyPrint: ").append(getPrettyPrint());
        return stringBuilder.toString();
    }

}