package pt.arquivo.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.arquivo.services.SearchQuery;
import pt.arquivo.services.SearchResult;
import pt.arquivo.services.SearchResult;

import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel
public class PageSearchResponse implements ApiResponse {

    private static Logger LOG = LoggerFactory.getLogger(PageSearchResponse.class);

    private String serviceName;
    private String linkToService;

    @JsonProperty("next_page")
    private String nextPage;

    @JsonProperty("previous_page")
    private String previousPage;

    @JsonIgnore
    private long totalItems;

    @JsonProperty("estimated_nr_results")
    private long estimatedNumberResults;

    @JsonProperty("request_parameters")
    private SearchQuery requestParameters;

    @JsonProperty("response_items")
    private ArrayList<SearchResult> responseItems;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getLinkToService() {
        return linkToService;
    }

    public void setLinkToService(String linkToService) {
        this.linkToService = linkToService;
    }

    public String getNextPage() {
        return nextPage;
    }

    public void setNextPage(String nextPage) {
        this.nextPage = nextPage;
    }

    public String getPreviousPage() {
        return previousPage;
    }

    public void setPreviousPage(String previousPage) {
        this.previousPage = previousPage;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public long getEstimatedNumberResults() {
        return estimatedNumberResults;
    }

    public void setEstimatedNumberResults(long estimatedNumberResults) {
        this.estimatedNumberResults = estimatedNumberResults;
    }

    public SearchQuery getRequestParameters() {
        return requestParameters;
    }

    public void setRequestParameters(SearchQuery requestParameters) {
        this.requestParameters = requestParameters;
    }

    public ArrayList<SearchResult> getResponseItems() {
        return responseItems;
    }

    public void setResponseItems(ArrayList<SearchResult> responseItems) {
        this.responseItems = responseItems;
    }

    public void setPagination(int maxItems, int offset, String queryString, boolean firstPage, boolean lastPage) {
        LOG.debug("setPagination parameters: maxItems %s, offset %s, queryString %s , firstPage %s, lastPage %s");

        int diffOffsetMaxItems = offset - maxItems;
        int previousOffset = (offset != 0 && diffOffsetMaxItems >= 0) ? (diffOffsetMaxItems) : 0;
        int nextOffset = offset + maxItems;

        if (!lastPage) {
            if (queryString.contains("offset=")) {
                String queryStringNextPage = queryString.replace("offset=" + offset, "offset=" + nextOffset);
                this.setNextPage(linkToService + "/textsearch?" + queryStringNextPage);
            } else {
                String queryStringNextPage = queryString.concat("&offset=" + nextOffset);
                this.setNextPage(linkToService + "/textsearch?" + queryStringNextPage); }
        }

        if (!firstPage) {
            if (queryString.contains("offset=")) {
                String queryStringPreviousPage = queryString.replace("offset=" + offset, "offset=" + previousOffset);
                this.setPreviousPage(linkToService + "/textsearch?" + queryStringPreviousPage);
            } else {
                this.setPreviousPage(linkToService + "/textsearch?" + queryString + "&offset=" + previousOffset);
            }
        }
    }
}
