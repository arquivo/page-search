package pt.arquivo.services;

import java.util.ArrayList;

public class SearchResults {

    private long numberEstimatedResults;
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

    public long getNumberEstimatedResults() {
        return numberEstimatedResults;
    }

    public void setNumberEstimatedResults(long numberEstimatedResults) {
        this.numberEstimatedResults = numberEstimatedResults;
    }

    public long getNumberResults() {
        return numberResults;
    }

    public void setNumberResults(long numberResults) {
        this.numberResults = numberResults;
    }
}
