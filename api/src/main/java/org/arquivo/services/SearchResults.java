package org.arquivo.services;

import java.util.ArrayList;

public class SearchResults {

    private long numberEstimatedResults;
    private long numberResults;
    private ArrayList<NutchWaxSearchResult> results;

    public ArrayList<NutchWaxSearchResult> getResults() {
        return results;
    }

    public void setResults(ArrayList<NutchWaxSearchResult> results) {
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
