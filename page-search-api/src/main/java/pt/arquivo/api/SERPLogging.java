package pt.arquivo.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.arquivo.services.SearchQuery;
import pt.arquivo.services.SearchResult;

import java.util.ArrayList;

public class SERPLogging {

    private static final Logger LOG = LoggerFactory.getLogger(SERPLogging.class);

    public static String logResult(long duration, String ipAddress, String userAgent, String urlRequest, PageSearchResponse pageSearchResponse, SearchQuery searchQuery) {
        StringBuilder serpLog = new StringBuilder();
        serpLog.append(ipAddress);
        serpLog.append("\t");
        serpLog.append(userAgent);
        serpLog.append("\t");
        serpLog.append(urlRequest);
        serpLog.append("\t");
        serpLog.append(duration);
        serpLog.append(" ms");
        serpLog.append("\tsearch_parameters: ");

        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonString = mapper.writeValueAsString(searchQuery);
            serpLog.append(jsonString);
            serpLog.append("\tsearch_results: ");
            ArrayList<String> results = new ArrayList<>();
            for (SearchResult searchResult : pageSearchResponse.getResponseItems()) {
                results.add(searchResult.getSearchResultId());
            }
            serpLog.append(mapper.writeValueAsString(results));
        } catch (JsonProcessingException e) {
            LOG.error("Something went wrong serializing object");
        }
        return serpLog.toString();
    }
}
