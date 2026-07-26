package pt.arquivo.services;

import java.util.ArrayList;

public class SearchResults {

    private long estimatedNumberResults;
    private long numberResults;
    private boolean lastPageResults = false;

    /** Spelling correction for the query, null when spellcheck wasn't requested or the query looks well spelled. */
    private String suggestedQuery;

    public String getSuggestedQuery() {
        return suggestedQuery;
    }

    public void setSuggestedQuery(String suggestedQuery) {
        this.suggestedQuery = suggestedQuery;
    }

    public boolean isLastPageResults() {
        return lastPageResults;
    }

    public void setLastPageResults(boolean lastPageResults) {
        this.lastPageResults = lastPageResults;
    }

    private ArrayList<SearchResult> results;

    public ArrayList<SearchResult> getResults() {
        return results;
    }

    public void setResults(ArrayList<SearchResult> results) {
        this.results = results;
    }

    public long getEstimatedNumberResults() {
        return estimatedNumberResults;
    }

    public void setEstimatedNumberResults(long estimatedNumberResults) {
        this.estimatedNumberResults = estimatedNumberResults;
    }

    public long getNumberResults() {
        return numberResults;
    }

    public void setNumberResults(long numberResults) {
        this.numberResults = numberResults;
    }
}
