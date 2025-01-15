package pt.arquivo.services;

public interface SearchService {
    SearchResults query(SearchQuery searchQuery);
    SearchResults query(SearchQuery searchQuery, boolean searchUrl);
    String getExtractedText(String urlTimestamp);
}
