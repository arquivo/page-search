package pt.arquivo.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.arquivo.services.SearchResult;
import pt.arquivo.services.SearchResults;
import pt.arquivo.services.SearchService;
import pt.arquivo.services.cdx.CDXSearchService;
import pt.arquivo.utils.Utils;


@Api(tags = "Metadata")
@RestController
public class MetadataController {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataController.class);

    @Value("${searchpages.api.servicename}")
    private String serviceName;

    @Value("${searchpages.service.link}")
    private String linkToService;

    @Autowired
    PageSearchController pageSearchController;

    @Autowired
    CDXSearchService cdxSearchService;

    @ApiOperation(value = "Get the metadata information about an archived page")
    @CrossOrigin
    @GetMapping(value = {"/metadata"})
    public MetadataResponse getMetadata(@RequestParam(value = "id") String id) {
        LOG.info(String.format("Request to /metadata for ID=%s", id));

        MetadataResponse metadataResponse = new MetadataResponse();
        // TODO Refactor
        int idx = id.lastIndexOf("/");
        if (idx > 0) {
            String[] versionIdSplited = {id.substring(0, idx), id.substring(idx + 1)};
            if (Utils.metadataValidator(versionIdSplited)) {
                SearchResults metadataSearchResults;
                SearchResults textSearchResults = pageSearchController.queryByUrl(versionIdSplited);
                SearchResults cdxSearchResults = cdxSearchService.getResults(versionIdSplited[0],
                        versionIdSplited[1], versionIdSplited[1], 1, 0);

                if (textSearchResults.getNumberResults() == 0) {
                    metadataSearchResults = cdxSearchResults;
                } else {
                    metadataSearchResults = textSearchResults;
                    if (cdxSearchResults.getResults().size() > 0) {
                        SearchResult textSearchResult = textSearchResults.getResults().get(0);
                        SearchResult cdxResult = (SearchResult) cdxSearchResults.getResults().get(0);
                        textSearchResult.setStatusCode(cdxResult.getStatusCode());

                        if (cdxResult.getCollection() != null && !cdxResult.getCollection().isEmpty())
                            textSearchResult.setCollection(cdxResult.getCollection());
                        textSearchResult.setContentLength(cdxResult.getContentLength());
                        textSearchResult.setOffset(cdxResult.getOffset());
                        textSearchResult.setFileName(cdxResult.getFileName());
                        textSearchResult.setDigest(cdxResult.getDigest());
                    }
                }
                metadataResponse.setLinkToService(linkToService);
                metadataResponse.setServiceName(serviceName);
                metadataResponse.setResponseItems(metadataSearchResults.getResults());
            }
        }

        return metadataResponse;
    }
}
