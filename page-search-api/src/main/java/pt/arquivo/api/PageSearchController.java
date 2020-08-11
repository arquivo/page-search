package pt.arquivo.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import pt.arquivo.api.exceptions.ApiNotFoundResourceException;
import pt.arquivo.api.exceptions.ApiRequestException;
import pt.arquivo.services.*;
import pt.arquivo.services.cdx.CDXSearchService;
import pt.arquivo.utils.Utils;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;


@Api(tags = "PageSearch")
@RestController
public class PageSearchController {

    private static final Logger LOG = LoggerFactory.getLogger(PageSearchController.class);

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

    @Autowired
    MetadataController metadataController;

    @ApiIgnore
    @CrossOrigin
    @GetMapping(value = {"/urlsearch/{url}"})
    public @ResponseBody
    ApiResponse searchCdxURL(@PathVariable String url,
                             @RequestParam(value = "from", required = false) String from,
                             @RequestParam(value = "to", required = false) String to,
                             @RequestParam(value = "maxItems", defaultValue = "50", required = false) int limit,
                             @RequestParam(value = "offset", defaultValue = "0", required = false) int offset,
                             HttpServletRequest request) {
        LOG.info(String.format("Request to urlsearch (versionHistory) for url=%s", url));
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

    @ApiOperation(value = "Get the extracted text of an Archived Page")
    @CrossOrigin
    @GetMapping(value = "/textextracted")
    public String extractedText(@RequestParam(value = "m") String id) {
        LOG.info(String.format("Request to /textextracted for ID=%s", id));
        String extractedText = "";

        int idx = id.lastIndexOf("/");
        if (idx > 0) {
            String[] versionIdSplited = {id.substring(0, idx), id.substring(idx + 1)};
            if (Utils.metadataValidator(versionIdSplited)) {
                SearchResults searchResults = queryByUrl(versionIdSplited);
                ArrayList<SearchResult> searchResultsArray = searchResults.getResults();
                if (searchResultsArray.size() > 0) {
                    extractedText = searchResultsArray.get(0).getExtractedText();
                    return extractedText;
                }
            }
        }
        throw new ApiNotFoundResourceException("Resource ID doesn't exist: " + id);
    }

    protected SearchResults queryByUrl(String[] versionIdSplited) {
        LOG.debug("Querying By Url with versionIdSplited as ", versionIdSplited);
        String url = versionIdSplited[0];
        String timeStamp = versionIdSplited[1];

        SearchQuery searchQuery = new SearchQueryImpl(url);
        searchQuery.setFrom(timeStamp);
        searchQuery.setTo(timeStamp);
        searchQuery.setMaxItems(1);

        return searchService.query(searchQuery, true);
    }


    @ApiOperation(value = "Search for Archived Pages that match the query parameters")
    @CrossOrigin
    @GetMapping(value = "/textsearch")
    public @ResponseBody
    ApiResponse pageSearch(@RequestParam(value = "q", required = false) String query,
                           @RequestParam(value = "versionHistory", required = false) String url,
                           @RequestParam(value = "metadata", required = false) String id,
                           @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
                           @RequestParam(value = "maxItems", required = false, defaultValue = "50") int maxItems,
                           @RequestParam(value = "siteSearch", required = false) String[] siteSearch,
                           @RequestParam(value = "dedupField", required = false, defaultValue = "site") String dedupField,
                           @RequestParam(value = "itemsPerSite", required = false) Integer itemsPerSite,
                           @RequestParam(value = "dedupValue", required = false, defaultValue = "2") int dedupValue,
                           @RequestParam(value = "from", required = false) String from,
                           @RequestParam(value = "to", required = false) String to,
                           @RequestParam(value = "type", required = false) String[] type,
                           @RequestParam(value = "collection", required = false) String[] collection,
                           @RequestParam(value = "fields", required = false) String[] fields,
                           @RequestParam(value = "prettyPrint", required = false) boolean prettyPrint,
                           HttpServletRequest request
    ) {
        // TODO need to do this verification since versionHistory is merged on the term search. Remove it on the next API version, when versionHistory is removed from here
        if (url != null) {
            return searchCdxURL(url, from, to, maxItems, offset, request);
        } else if (id != null) {
            return metadataController.getMetadata(id);

        } else if (query == null) {
            LOG.error("Invalid API Request " + request.getQueryString());
            throw new ApiRequestException("Invalid API Request");
        }

        SearchQueryImpl searchQuery = new SearchQueryImpl(query);
        searchQuery.setOffset(offset);
        searchQuery.setMaxItems(maxItems);

        // TODO to decrepate parameter
        if (itemsPerSite != null) {
            searchQuery.setLimitPerSite(itemsPerSite);
        }
        if (from != null) {
            searchQuery.setFrom(from);
        }
        if (to != null) {
            searchQuery.setTo(to);
        }
        searchQuery.setType(type);
        searchQuery.setSite(siteSearch);
        searchQuery.setCollection(collection);
        searchQuery.setFields(fields);
        searchQuery.setPrettyPrint(prettyPrint);

        searchQuery.setDedupValue(dedupValue);
        if (request.getParameter("dedupField") == null && searchQuery.isSearchBySite()) {
            searchQuery.setDedupField("url");
        } else {
            searchQuery.setDedupField(dedupField);
        }

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
            LOG.debug("prettyPrint request");
            jacksonObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        } else {
            jacksonObjectMapper.disable(SerializationFeature.INDENT_OUTPUT);
        }
        StringBuffer requestUrl = request.getRequestURL();
        if (request.getQueryString() != null) {
            requestUrl.append("?");
            requestUrl.append(request.getQueryString());
        }
        LOG.info(SERPLogging.logResult(requestUrl.toString(), pageSearchResponse, searchQuery));
        return pageSearchResponse;
    }

}
