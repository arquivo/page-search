package pt.arquivo.services;

import java.util.ArrayList;

public class SearchResults {

    private long estimatedNumberResults;
    private long numberResults;
    private boolean lastPageResults = false;

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
