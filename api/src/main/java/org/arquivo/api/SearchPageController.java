package org.arquivo.api;

import org.springframework.web.bind.annotation.*;

@RestController
public class SearchPageController {

    // @RequestMapping(value = "/textsearch", method = {RequestMethod.GET, RequestMethod.POST})
    // public @ResponseBody
    // SearchResult pageSearch(TextSearchRequestParameters searchQuery) {
    //     // TODO test if this is building the right object
    //     SearchResult searchResult = new SearchResult();
    //     return searchResult;
    // }

    // TODO default values to configuration file
    @RequestMapping(value = "/textsearch", method = {RequestMethod.GET})
    public @ResponseBody
    SearchResult pageSearch(@RequestParam(value = "q", required = true) String query,
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

        TextSearchRequestParameters textSearchRequestParameters = new TextSearchRequestParameters(query);
        // TextSearchRequestParameters textSearchRequestParameters = new TextSearchRequestParameters(query, offset,
        //         maxItems, itemsPerSite, siteSearch, limit, limitPerSite, sort, from, collection, prettyPrint);


        // TODO call SearchService
        SearchResult searchResult = new SearchResult();
        searchResult.setRequestParameters(textSearchRequestParameters);
        return searchResult;
    }
}
