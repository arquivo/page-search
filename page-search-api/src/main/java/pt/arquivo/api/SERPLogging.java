package pt.arquivo.api;

import pt.arquivo.services.SearchQuery;
import pt.arquivo.services.SearchResult;

import java.util.ArrayList;

public class SERPLogging {
    public static String logResult(String searchId, PageSearchResponse pageSearchResponse, SearchQuery searchQuery) {
        StringBuilder serpLog = new StringBuilder();
        serpLog.append("SERP_ID: ");
        serpLog.append(searchId);
        serpLog.append(" REQUEST: [");
        serpLog.append(searchQuery + "]");
        serpLog.append(" RESULTS: [");
        ArrayList<SearchResult> arrayList = pageSearchResponse.getResponseItems();
        for (SearchResult searchResult : arrayList) {
            String resultId = searchResult.getSearchResultId();
            serpLog.append(" " + resultId);
        }
        serpLog.append("]");
        return serpLog.toString();
    }
}
