package pt.arquivo.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.StringUtils;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TitleSearchQueryImpl extends SearchQueryImpl implements TitleSearchQuery {

    public TitleSearchQueryImpl(String title, String queryTerms) {
        super(queryTerms);
        setTitle(title);
    }

    @JsonProperty("title")
    private String title;

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("title: ").append(getTitle());
        stringBuilder.append("q: ").append(getQueryTerms());
        stringBuilder.append(" offset: ").append(getOffset());
        stringBuilder.append(" maxItems: ").append(getMaxItems());
        stringBuilder.append(" from: ").append(getFrom());
        stringBuilder.append(" to: ").append(getTo());
        stringBuilder.append(" type: ").append(getType());
        stringBuilder.append(" collection: ").append(getCollection());
        stringBuilder.append(" prettyPrint: ").append(getPrettyPrint());
        return stringBuilder.toString();
    }

}