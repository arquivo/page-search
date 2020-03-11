package org.arquivo.services;

import java.io.IOException;
import java.util.ArrayList;

public interface SearchService {

    public ArrayList<SearchResult> query(SearchQuery searchQuery);
}
