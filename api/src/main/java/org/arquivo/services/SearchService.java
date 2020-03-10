package org.arquivo.services;

import org.arquivo.api.SearchQuery;

import java.util.ArrayList;

public interface SearchService {

    public ArrayList<SearchResult> query(SearchQuery searchQuery);
}
