package org.arquivo.services;

import org.arquivo.api.SearchQuery;

import java.util.ArrayList;

// SHOULD HAVE THE SAME INTERFACE AS NutchWaxService
public class SolrSearchService implements SearchService {

    @Override
    public ArrayList<SearchResult> query(SearchQuery searchQuery) {
        return null;
    }
}
