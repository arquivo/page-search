package pt.arquivo.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import pt.arquivo.services.SearchQuery;
import pt.arquivo.services.SearchResult;

import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchPageResponse {

    private String serviceName;
    private String linkToService;

    @JsonProperty("next_page")
    private String nextPage;

    @JsonProperty("previous_page")
    private String previousPage;
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
}
