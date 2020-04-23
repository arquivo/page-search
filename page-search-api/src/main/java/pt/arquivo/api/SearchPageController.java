package pt.arquivo.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import pt.arquivo.services.*;
import pt.arquivo.services.cdx.CDXSearchService;
import pt.arquivo.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.regex.Pattern;

@RestController
public class SearchPageController {

    private static final Log LOG = LogFactory.getLog(SearchPageController.class);

    @Value("${searchpages.api.servicename}")
    private String serviceName;

    @Value("${searchpages.service.link}")
    private String linkToService;

    @Autowired
    private CDXSearchService cdxSearchService;

    @Autowired
    private SearchService searchService;

    public @ResponseBody
    SearchPageResponse getMetadata() {
        return null;
    }

    @RequestMapping(value = {"/urlsearch"}, method = {RequestMethod.GET})
    public @ResponseBody
    SearchPageResponse searchUrl(@RequestParam(value = "url") String url,
                                 @RequestParam(value = "from", required = false) String from,
                                 @RequestParam(value = "to", required = false) String to,
                                 @RequestParam(value = "maxItems", required = false) int limit,
                                 @RequestParam(value = "offset", required = false) int start,
                                    HttpServletRequest request) {

        SearchPageResponse searchPageResponse = new SearchPageResponse();
        SearchResults searchResults = cdxSearchService.getResults(url, from, to, limit, start);
        searchPageResponse.setResponseItems(searchResults.getResults());

        searchPageResponse.setServiceName(serviceName);
        searchPageResponse.setLinkToService(linkToService);

        setPagination(limit, start, request.getQueryString(), searchPageResponse, true, true);

        return searchPageResponse;
    }

    @RequestMapping(value = "/extractedtext", method = {RequestMethod.GET})
    public String extractedText(@RequestParam(value = "m") String metadata) {
        String extractedText = "";

        String versionId = metadata.split(" ")[0];
        int idx = versionId.indexOf("/");
        if (idx > 0) {
            String[] versionIdSplited = {versionId.substring(0, idx), versionId.substring(idx + 1)};
            if (metadataValidator(versionIdSplited)) {

                // TODO should use the waybackQuery = true
                String extractUrl = versionIdSplited[1];

                String dateLucene = "date:".concat(versionIdSplited[0].concat(" "));
                String qLucene = dateLucene.concat(extractUrl);

                SearchQuery searchQuery = new SearchQueryImpl(qLucene);
                searchQuery.setLimit(1);

                SearchResults searchResults = searchService.query(searchQuery);

                ArrayList<SearchResult> searchResultsArray = searchResults.getResults();
                extractedText = searchResultsArray.get(0).getExtractedText();
            }
        }
        return extractedText;
    }

    // TODO default values to configuration file
    @CrossOrigin
    @RequestMapping(value = "/textsearch", method = {RequestMethod.GET})
    public @ResponseBody
    SearchPageResponse pageSearch(@RequestParam(value = "q", required = false) String query,
                                  @RequestParam(value = "versionHistory", required = false) String url,
                                  @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
                                  @RequestParam(value = "maxItems", required = false, defaultValue = "50") int maxItems,
                                  @RequestParam(value = "siteSearch", required = false) String[] siteSearch,
                                  @RequestParam(value = "limitPerSite", required = false, defaultValue = "2") int limitPerSite,
                                  @RequestParam(value = "from", required = false) String from,
                                  @RequestParam(value = "to", required = false) String to,
                                  @RequestParam(value = "type", required = false) String type,
                                  @RequestParam(value = "collection", required = false) String collection,
                                  @RequestParam(value = "fields", required = false) String[] fields,
                                  @RequestParam(value = "prettyPrint", required = false) String prettyPrint,
                                  HttpServletRequest request
    ) {

        // TODO need to do this verification since versionHistory is merged on the term search.... what a nice idea... remove it on the next API version, when versionHistory is removed from here
        if (url != null) {
            return searchUrl(url, from, to, maxItems, offset, request);
        } else if (query == null) {
            // TODO break API with illegal call
        }

        SearchQueryImpl searchQuery = new SearchQueryImpl(query, offset, maxItems, limitPerSite, from, to, type,
                siteSearch, collection, fields, prettyPrint);

        SearchResults searchResults;
        searchResults = searchService.query(searchQuery);

        SearchPageResponse searchPageResponse = new SearchPageResponse();

        searchPageResponse.setServiceName(serviceName);
        searchPageResponse.setLinkToService(linkToService);

        searchPageResponse.setRequestParameters(searchQuery);
        searchPageResponse.setResponseItems(searchResults.getResults());
        searchPageResponse.setEstimatedNumberResults(searchResults.getNumberEstimatedResults());
        searchPageResponse.setTotalItems(searchResults.getNumberResults());

        boolean lastPage = searchResults.isLastPageResults();
        boolean firstPage = offset <= 0;

        String queryString = request.getQueryString();
        setPagination(maxItems, offset, queryString, searchPageResponse, firstPage, lastPage);

        return searchPageResponse;
    }

    private void setPagination(int maxItems, int offset, String queryString, SearchPageResponse searchPageResponse,
                               boolean firstPage, boolean lastPage) {

        int diffOffsetMaxItems = offset - maxItems;
        int previousOffset = (offset != 0 && diffOffsetMaxItems >= 0) ? (diffOffsetMaxItems) : 0;
        int nextOffset = offset + maxItems;

        if (!lastPage) {
            if (queryString.contains("offset=")) {
                String queryStringNextPage = queryString.replace("offset=" + offset, "offset=" + nextOffset);
                searchPageResponse.setNextPage(linkToService + "/textsearch?" + queryStringNextPage);
            } else {
                String queryStringNextPage = queryString.concat("&offset=" + nextOffset);
                searchPageResponse.setNextPage(linkToService + "/textsearch?" + queryStringNextPage);
            }
        }

        if (!firstPage) {
            if (queryString.contains("offset=")) {
                String queryStringPreviousPage = queryString.replace("offset=" + offset, "offset=" + previousOffset);
                searchPageResponse.setPreviousPage(linkToService + "/textsearch?" + queryStringPreviousPage);
            } else {
                searchPageResponse.setPreviousPage(linkToService + "/textsearch?" + queryString + "&offset=" + previousOffset);
            }
        }
    }

    private static boolean metadataValidator(String[] versionIdsplited) {
        LOG.info("metadata versionId[0][" + versionIdsplited[0] + "] versionId[1][" + versionIdsplited[1] + "]");
        if (Utils.urlValidator(versionIdsplited[1]) && versionIdsplited[0].matches("[0-9]+"))
            return true;
        else
            return false;
    }
}
