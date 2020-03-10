package org.arquivo.api;

import org.arquivo.services.NutchWaxSearchService;
import org.arquivo.services.SearchResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
public class SearchPageController {

    // TODO default values to configuration file
    @RequestMapping(value = "/textsearch", method = {RequestMethod.GET})
    public @ResponseBody
    SearchResultResponse pageSearch(@RequestParam(value = "q", required = true) String query,
                                    @RequestParam(value = "offset", required = false, defaultValue = "0") String offset,
                                    @RequestParam(value = "maxItems", required = false, defaultValue = "50") String maxItems,
                                    @RequestParam(value = "itemsPerSite", required = false, defaultValue = "2") String itemsPerSite,
                                    @RequestParam(value = "siteSearch", required = false, defaultValue = "") String siteSearch,
                                    @RequestParam(value = "limitPerSite", required = false, defaultValue = "2") String limitPerSite,
                                    @RequestParam(value = "from", required = false, defaultValue = "1996") String from,
                                    @RequestParam(value = "to", required = false, defaultValue = "-1") String to,
                                    @RequestParam(value = "type", required = false) String type,
                                    @RequestParam(value = "collection", required = false) String collection,
                                    @RequestParam(value = "fields", required = false) String fields,
                                    @RequestParam(value = "prettyPrint", required = false) String prettyPrint
    ) {
        NutchWaxSearchService searchService = new NutchWaxSearchService();

        SearchQuery searchQuery = new SearchQuery(query);
        ArrayList<SearchResult> searchResults = searchService.query(searchQuery);

        SearchResultResponse searchResultResponse = new SearchResultResponse();

        // TODO get this information from configuration file
        searchResultResponse.setServiceName("tomates");
        searchResultResponse.setTotalItems(1);
        searchResultResponse.setResponseItems(searchResults);

        return searchResultResponse;
    }
}
