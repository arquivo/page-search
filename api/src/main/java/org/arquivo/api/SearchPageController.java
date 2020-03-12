package org.arquivo.api;

import org.arquivo.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

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
                ArrayList<NutchWaxSearchResult> searchResultsArray = searchResults.getResults();
                extractedText = searchResultsArray.get(0).getExtractedText();
            }
        }
        return extractedText;
    }

    // TODO default values to configuration file
    @RequestMapping(value = "/textsearch", method = {RequestMethod.GET})
    public @ResponseBody
    SearchResultResponse pageSearch(@RequestParam(value = "q") String query,
                                    @RequestParam(value = "offset", required = false, defaultValue = "0") String offset,
                                    @RequestParam(value = "maxItems", required = false, defaultValue = "50") String maxItems,
                                    @RequestParam(value = "siteSearch", required = false, defaultValue = "") String siteSearch,
                                    @RequestParam(value = "limitPerSite", required = false, defaultValue = "2") String limitPerSite,
                                    @RequestParam(value = "from", required = false) String from,
                                    @RequestParam(value = "to", required = false) String to,
                                    @RequestParam(value = "type", required = false) String type,
                                    @RequestParam(value = "collection", required = false) String collection,
                                    @RequestParam(value = "fields", required = false) String fields,
                                    @RequestParam(value = "prettyPrint", required = false) String prettyPrint
    ) {

        NutchWaxSearchQuery searchQuery = new NutchWaxSearchQuery(query, offset, maxItems, limitPerSite, from, to, type,
                siteSearch, collection, fields, prettyPrint);

        SearchResults searchResults;
        searchResults = searchService.query(searchQuery);

        SearchResultResponse searchResultResponse = new SearchResultResponse();

        searchResultResponse.setServiceName(serviceName);
        searchResultResponse.setLinkToService(linkToService);

        searchResultResponse.setRequestParameters(searchQuery);
        searchResultResponse.setResponseItems(searchResults.getResults());
        searchResultResponse.setEstimatedNumberResults(searchResults.getNumberEstimatedResults());
        searchResultResponse.setTotalItems(searchResults.getNumberResults());

        return searchResultResponse;
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
