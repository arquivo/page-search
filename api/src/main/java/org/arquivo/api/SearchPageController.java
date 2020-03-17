package org.arquivo.api;

import org.arquivo.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.regex.Pattern;

@RestController
public class SearchPageController {

    @Value("${searchpages.api.servicename}")
    private String serviceName;

    @Value("${searchpages.service.link}")
    private String linkToService;

    @Autowired
    private SearchService searchService;


    // TODO why not use EXACTURL ????
    @RequestMapping(value = "/extractedtext")
    public String extractedText(@RequestParam(value = "m") String metadata) {
        String extractedText = "";

        String versionId = metadata.split(" ")[0];
        int idx = versionId.indexOf("/");
        if (idx > 0) {
            String[] versionIdSplited = {versionId.substring(0, idx), versionId.substring(idx + 1)};
            if (metadataValidator(versionIdSplited)) {

                String extractUrl = versionIdSplited[1];

                String dateLucene = "date:".concat(versionIdSplited[0].concat(" "));
                String qLucene = dateLucene.concat(extractUrl);

                SearchQuery searchQuery = new NutchWaxSearchQuery(qLucene);
                // FIXME no sense being a string
                searchQuery.setLimit("1");

                SearchResults searchResults = searchService.query(searchQuery);

                // SANITY CHECK ONLY 1 RESULT
                ArrayList<SearchResult> searchResultsArray = searchResults.getResults();
                extractedText = searchResultsArray.get(0).getExtractedText();
            }
        }
        return extractedText;
    }

    // TODO default values to configuration file
    @RequestMapping(value = "/textsearch", method = {RequestMethod.GET})
    public @ResponseBody
    SearchPageResponse pageSearch(@RequestParam(value = "q") String query,
                                  @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
                                  @RequestParam(value = "maxItems", required = false, defaultValue = "50") int maxItems,
                                  @RequestParam(value = "siteSearch", required = false) String siteSearch,
                                  @RequestParam(value = "limitPerSite", required = false, defaultValue = "2") int limitPerSite,
                                  @RequestParam(value = "from", required = false) String from,
                                  @RequestParam(value = "to", required = false) String to,
                                  @RequestParam(value = "type", required = false) String type,
                                  @RequestParam(value = "collection", required = false) String collection,
                                  @RequestParam(value = "fields", required = false) String[] fields,
                                  @RequestParam(value = "prettyPrint", required = false) String prettyPrint,
                                  HttpServletRequest request
    ) {

        NutchWaxSearchQuery searchQuery = new NutchWaxSearchQuery(query, offset, maxItems, limitPerSite, from, to, type,
                siteSearch, collection, fields, prettyPrint);

        SearchResults searchResults;
        searchResults = searchService.query(searchQuery);

        SearchPageResponse searchPageResponse = new SearchPageResponse();

        searchPageResponse.setServiceName(serviceName);
        searchPageResponse.setLinkToService(linkToService);

        String queryString = request.getQueryString();
        setPagination(maxItems, offset, queryString, searchPageResponse);


        searchPageResponse.setRequestParameters(searchQuery);
        searchPageResponse.setResponseItems(searchResults.getResults());
        searchPageResponse.setEstimatedNumberResults(searchResults.getNumberEstimatedResults());
        searchPageResponse.setTotalItems(searchResults.getNumberResults());

        return searchPageResponse;
    }

    private void setPagination(int maxItems, int offset, String queryString, SearchPageResponse searchPageResponse) {

        if (queryString.contains("offset=")){
            String queryStringNextPage = queryString.replace("offset=" + offset, "offset=" + (offset + maxItems));
            searchPageResponse.setNextPage(linkToService + "/textsearch?" + queryStringNextPage);


            String queryStringPreviousPage = queryString.replace("offset=" + offset, "offset="
                    + ((offset != 0) ? (offset - maxItems) : 0));
            searchPageResponse.setPreviousPage(linkToService + "/textsearch?" + queryStringPreviousPage);
        }
        else {
            searchPageResponse.setNextPage(linkToService + "/textsearch?" + queryString + "&offset=" + maxItems);
            searchPageResponse.setPreviousPage(linkToService + "/textsearch?" + queryString + "&offset=0");
        }
    }

    /**
     * Check if parameter url is URL
     *
     * @param url
     * @return
     */
    private static boolean urlValidator(String url) {
        Pattern URL_PATTERN = Pattern.compile("^.*? ?((https?:\\/\\/)?([a-zA-Z\\d][-\\w\\.]+)\\.([a-z\\.]{2,6})([-\\/\\w\\p{L}\\.~,;:%&=?+$#*]*)*\\/?) ?.*$");
        return URL_PATTERN.matcher(url).matches();
    }

    /**
     * Check if parameter versionId is format url/tstamp
     *
     * @param versionIdsplited
     * @return
     */
    private static boolean metadataValidator(String[] versionIdsplited) {
        // TODO LOG.info( "metadata versionId[0]["+versionIdsplited[ 0 ]+"] versionId[1]["+versionIdsplited[ 1 ]+"]" );
        if (urlValidator(versionIdsplited[1]) && versionIdsplited[0].matches("[0-9]+"))
            return true;
        else
            return false;
    }
}
