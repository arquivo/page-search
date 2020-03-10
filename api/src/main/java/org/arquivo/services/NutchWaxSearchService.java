package org.arquivo.services;

import org.arquivo.api.SearchQuery;

import java.util.ArrayList;

public class NutchWaxSearchService implements SearchService {

    @Override
    public ArrayList<SearchResult> query(SearchQuery searchQuery) {
        ArrayList<SearchResult> searchResults = new ArrayList<>();
        SearchResult searchResult = new SearchResult();
        searchResult.setTitle("TESTE");
        searchResults.add(searchResult);
        return searchResults;
    }
}
