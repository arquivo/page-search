package pt.arquivo.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

@RestController
public class PageSearchController {

    private static final Log LOG = LogFactory.getLog(PageSearchController.class);

    @Value("${searchpages.api.servicename}")
    private String serviceName;

    @Value("${searchpages.service.link}")
    private String linkToService;

    @Autowired
    private CDXSearchService cdxSearchService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @CrossOrigin
    @GetMapping(value = {"/urlsearch/{url}"})
    public @ResponseBody
    ApiResponse searchUrl(@PathVariable String url,
                          @RequestParam(value = "from", required = false) String from,
                          @RequestParam(value = "to", required = false) String to,
                          @RequestParam(value = "maxItems", defaultValue = "50", required = false) int limit,
                          @RequestParam(value = "offset", defaultValue = "0", required = false) int offset,
                          HttpServletRequest request) {

        SearchResults searchResults = cdxSearchService.getResults(url, from, to, limit, offset);

        PageSearchResponse pageSearchResponse = new PageSearchResponse();
        pageSearchResponse.setResponseItems(searchResults.getResults());
        pageSearchResponse.setEstimatedNumberResults(searchResults.getEstimatedNumberResults());
        pageSearchResponse.setTotalItems(searchResults.getNumberResults());

        pageSearchResponse.setServiceName(serviceName);
        pageSearchResponse.setLinkToService(linkToService);

        boolean firstPage = offset <= 0;
        boolean lastPage = searchResults.isLastPageResults();

        pageSearchResponse.setPagination(limit, offset, request.getQueryString(), firstPage, lastPage);

        return pageSearchResponse;
    }

    @CrossOrigin
    @GetMapping(value = "/textextracted")
    public String extractedText(@RequestParam(value = "m") String id) {
        LOG.info("Getting extracted: " + id);
        String extractedText = "";

        int idx = id.lastIndexOf("//");
        if (idx > 0) {
            String[] versionIdSplited = {id.substring(0, idx + 1), id.substring(idx + 2)};
            if (metadataValidator(versionIdSplited)) {

                SearchResults searchResults = queryByUrl(versionIdSplited);

                ArrayList<SearchResult> searchResultsArray = searchResults.getResults();
                extractedText = searchResultsArray.get(0).getExtractedText();
            }
        }
        return extractedText;
    }

    private SearchResults queryByUrl(String[] versionIdSplited) {
        String url = versionIdSplited[0];
        String timeStamp = versionIdSplited[1];

        SearchQuery searchQuery = new SearchQueryImpl(url);
        searchQuery.setFrom(timeStamp);
        searchQuery.setTo(timeStamp);
        searchQuery.setLimit(1);

        return searchService.query(searchQuery, true);
    }

    @CrossOrigin
    @GetMapping(value = {"/metadata"})
    public ApiResponse getMetadata(@RequestParam(value = "metadata") String id) {
        LOG.info("Getting metadata for: " + id);
        ArrayList<SearchResult> searchResultsArray = new ArrayList<>();

        int idx = id.lastIndexOf("//");
        if (idx > 0) {
            String[] versionIdSplited = {id.substring(0, idx + 1), id.substring(idx + 2)};
            if (metadataValidator(versionIdSplited)) {
                SearchResults searchResults = queryByUrl(versionIdSplited);
                searchResultsArray = searchResults.getResults();
            }
        }

        MetadataResponse metadataResponse = new MetadataResponse();
        metadataResponse.setLinkToService(linkToService);
        metadataResponse.setServiceName(serviceName);
        metadataResponse.setResponseItems(searchResultsArray);

        return metadataResponse;
    }

    @CrossOrigin
    @GetMapping(value = "/textsearch")
    public @ResponseBody
    ApiResponse pageSearch(@RequestParam(value = "q", required = false) String query,
                           @RequestParam(value = "versionHistory", required = false) String url,
                           @RequestParam(value = "metadata", required = false) String id,
                           @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
                           @RequestParam(value = "maxItems", required = false, defaultValue = "50") int maxItems,
                           @RequestParam(value = "siteSearch", required = false) String[] siteSearch,
                           @RequestParam(value = "limitPerSite", required = false, defaultValue = "2") int limitPerSite,
                           @RequestParam(value = "from", required = false) String from,
                           @RequestParam(value = "to", required = false) String to,
                           @RequestParam(value = "type", required = false) String type,
                           @RequestParam(value = "collection", required = false) String collection,
                           @RequestParam(value = "fields", required = false) String[] fields,
                           @RequestParam(value = "prettyPrint", required = false) boolean prettyPrint,
                           HttpServletRequest request
    ) {

        // TODO need to do this verification since versionHistory is merged on the term search.... what a nice idea... remove it on the next API version, when versionHistory is removed from here
        if (url != null) {
            return searchUrl(url, from, to, maxItems, offset, request);
        } else if (id != null) {
            return getMetadata(id);
        } else if (query == null) {
            // TODO break API with illegal call
        }

        SearchQueryImpl searchQuery = new SearchQueryImpl(query, offset, maxItems, limitPerSite, from, to, type,
                siteSearch, collection, fields, prettyPrint);

        SearchResults searchResults;
        searchResults = searchService.query(searchQuery);

        PageSearchResponse pageSearchResponse = new PageSearchResponse();

        pageSearchResponse.setServiceName(serviceName);
        pageSearchResponse.setLinkToService(linkToService);

        pageSearchResponse.setRequestParameters(searchQuery);
        pageSearchResponse.setResponseItems(searchResults.getResults());
        pageSearchResponse.setEstimatedNumberResults(searchResults.getEstimatedNumberResults());
        pageSearchResponse.setTotalItems(searchResults.getNumberResults());

        boolean lastPage = searchResults.isLastPageResults();
        boolean firstPage = offset <= 0;

        String queryString = request.getQueryString();
        pageSearchResponse.setPagination(maxItems, offset, queryString, firstPage, lastPage);

        if (searchQuery.getPrettyPrint()) {
            jacksonObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        } else {
            jacksonObjectMapper.disable(SerializationFeature.INDENT_OUTPUT);
        }
        return pageSearchResponse;
    }


    private static boolean metadataValidator(String[] versionIdsplited) {
        LOG.debug("metadata versionId[0][" + versionIdsplited[0] + "] versionId[1][" + versionIdsplited[1] + "]");
        if (Utils.urlValidator(versionIdsplited[0]) && versionIdsplited[1].matches("[0-9]+"))
            return true;
        else
            return false;
    }
}
