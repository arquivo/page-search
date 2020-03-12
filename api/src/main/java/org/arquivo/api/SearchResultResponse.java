package org.arquivo.api;

import org.arquivo.services.NutchWaxSearchQuery;
import org.arquivo.services.NutchWaxSearchResult;

import java.util.ArrayList;

public class SearchResultResponse {

    private String serviceName;
    private String linkToService;
    private String nextPage;
    private String previousPage;
    private long totalItems;
    private long estimatedNumberResults;
    private NutchWaxSearchQuery requestParameters;

    private ArrayList<NutchWaxSearchResult> responseItems;

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

    public NutchWaxSearchQuery getRequestParameters() {
        return requestParameters;
    }

    public void setRequestParameters(NutchWaxSearchQuery requestParameters) {
        this.requestParameters = requestParameters;
    }

    public ArrayList<NutchWaxSearchResult> getResponseItems() {
        return responseItems;
    }

    public void setResponseItems(ArrayList<NutchWaxSearchResult> responseItems) {
        this.responseItems = responseItems;
    }
}
