package pt.arquivo.services.nutchwax;

import org.apache.hadoop.conf.Configuration;
import org.apache.lucene.search.PwaFunctionsWritable;
import org.apache.nutch.global.Global;
import org.apache.nutch.html.Entities;
import org.apache.nutch.searcher.*;
import org.archive.access.nutch.NutchwaxBean;
import org.archive.access.nutch.NutchwaxConfiguration;
import org.archive.access.nutch.jobs.EntryPageExpansion;
import org.archive.util.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import pt.arquivo.services.*;
import pt.arquivo.utils.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class NutchWaxSearchService implements SearchService {

    private static final Logger LOG = LoggerFactory.getLogger(NutchWaxSearchService.class);

    private final int searcherMaxHits;
    private Configuration conf;
    private NutchwaxBean bean;

    @Value("${searchpages.api.startdate:19960101000000}")
    private String startDate;

    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    @Value("${searchpages.service.link:http://localhost:8081}")
    private String serviceName;

    @Value("${screenshot.service.endpoint:https://preprod.arquivo.pt/screenshot}")
    private String screenshotServiceEndpoint;

    @Value("${wayback.service.endpoint:https://preprod.arquivo.pt/wayback}")
    private String waybackServiceEndpoint;

    @Value("${wayback.noframe.service.endpoint:https://preprod.arquivo.pt/noFrame/replay}")
    private String waybackNoFrameServiceEndpoint;

    @Value("${searchpages.extractedtext.service.link:http://localhost:8081/textextracted}")
    private String extractedTextServiceEndpoint;

    @Value("${searchpages.textsearch.service.link:http://localhost:8081/textsearch}")
    private String textSearchServiceEndpoint;

    @Value("${searchpages.api.show.ids:false}")
    private boolean showIds;

    public NutchWaxSearchService(Configuration conf) throws IOException {
        this.conf = conf;
        this.bean = new NutchwaxBean(conf);
        this.searcherMaxHits = Integer.parseInt(this.conf.get(Global.MAX_FULLTEXT_MATCHES_RETURNED));
    }

    public NutchWaxSearchService() throws IOException {
        this.conf = NutchwaxConfiguration.getConfiguration();
        conf.addFinalResource("wax-default.xml");

        String searchFilePathEnv = System.getenv("NUTCHWAX_SEARCH_FILE");
        if (searchFilePathEnv != null) {
            conf.set("searcher.dir", searchFilePathEnv);
        }

        this.bean = new NutchwaxBean(conf);
        this.searcherMaxHits = Integer.parseInt(this.conf.get(Global.MAX_FULLTEXT_MATCHES_RETURNED));
    }

    @Override
    public SearchResults query(SearchQuery searchQuery, boolean searchUrl) {
        if (searchUrl) {
            String queryTerms = searchQuery.getQueryTerms();
            try {
                searchQuery.setQueryTerms(encodeVersionHistory(queryTerms));
                return query(searchQuery);
            } catch (NoSuchAlgorithmException e) {
                LOG.error("EncodeVersionHistory thrown a up a error", e);
            }
        }
        SearchResults searchResults = new SearchResults();
        searchResults.setNumberResults(0);
        searchResults.setEstimatedNumberResults(0);
        return searchResults;
    }

    private String buildNutchwaxQueryString(SearchQuery searchQuery) {
        StringBuilder queryString = new StringBuilder();

        if (searchQuery.isTimeBoundedQuery()) {
            if (searchQuery.getFrom() == null) {
                searchQuery.setFrom(startDate);
            }

            if (searchQuery.getTo() == null) {
                Date endDate = new Date();
                searchQuery.setTo(dateFormat.format(endDate));
            }
            queryString.append("date:".concat(searchQuery.getFrom()).concat("-").concat(searchQuery.getTo()));
        }

        String queryTerms = searchQuery.getQueryTerms();
        queryString.append(" ".concat(queryTerms));

        if (searchQuery.getOffset() >= searcherMaxHits) {
            searchQuery.setOffset(searcherMaxHits);
        }

        if (searchQuery.isSearchBySite()) {
            for (int i = 0; i < searchQuery.getSite().length; i++) {
                LOG.debug("siteP = " + searchQuery.getSite()[i]);
                String site = " site:".concat(searchQuery.getSite()[i]);
                site = site.replaceAll("site:http://", "site:");
                site = site.replaceAll("site:https://", "site:");
                queryString.append(site);
            }
        }

        if (searchQuery.isSearchByType()) {
            for (int i = 0; i < searchQuery.getType().length; i++) {
                LOG.debug("type = " + searchQuery.getType()[i]);
                String type = " type:".concat(searchQuery.getType()[i]);
                queryString.append(type);
            }
        }

        if (searchQuery.isSearchByCollection()) {
            for (int i = 0; i < searchQuery.getCollection().length; i++) {
                LOG.debug("collection = " + searchQuery.getCollection()[i]);
                String collection = " collection:".concat(searchQuery.getCollection()[i]);
                queryString.append(collection);
            }
        }

        return queryString.toString();
    }

    public static boolean isLastPage(int numberOfResults, SearchQuery searchQuery) {
        return numberOfResults <= searchQuery.getOffset() + searchQuery.getMaxItems();
    }


    @Override
    public SearchResults query(SearchQuery searchQuery) {
        SearchResults results = new SearchResults();
        ArrayList<SearchResult> searchResults = new ArrayList<>();

        boolean urlSearchQuery = Utils.urlValidator(searchQuery.getQueryTerms().split(" ")[0]) || searchQuery.getQueryTerms().contains("exacturl:");

        if (searchQuery.isSearchBySite() && searchQuery.getDedupField() == null) {
            searchQuery.setDedupField("url");
        } else if (searchQuery.getDedupField() == null) {
            searchQuery.setDedupField("site");
        }

        int numberOfHits = searchQuery.getOffset() + searchQuery.getMaxItems();

        String nutchwaxQueryString = buildNutchwaxQueryString(searchQuery);

        try {
            Query query = Query.parse(nutchwaxQueryString, conf);
            LOG.info(String.format("Executing query - queryString: %s numberOfHits: %s, " +
                            "searcherMaxHits: %s, hitsPerDup: %s, urlSearchQuery: %s", query, numberOfHits,
                    searcherMaxHits, searchQuery.getDedupValue(), urlSearchQuery));

            /*
                Query for numberOfHits + 1 so we can determine that its the last page with results.
                when dedupValue == 0, there is not deduplication, therefore nutchwax doesn't apply
                the numHitsRaw multiply factor, which makes sense because you don't really need to clean up results.
                The problem is that then we are not able to know if this was the last page of results or not.

            */
            if (searchQuery.getDedupValue() == 0){
                numberOfHits = numberOfHits + 1;
            }
            // don't get deceive by the maxHitsPerVersion, it doesn't work
            Hits hits = bean.search(query, numberOfHits, searcherMaxHits,
                    searchQuery.getDedupValue(), searchQuery.getDedupField(), null, false,
                    PwaFunctionsWritable.parse(conf.get(Global.RANKING_FUNCTIONS)), 1, urlSearchQuery);
            results.setLastPageResults(isLastPage(hits.getLength(), searchQuery));
            results.setEstimatedNumberResults(hits.getTotal());

            // build SearchResults
            if (hits.getLength() >= 1) {
                int end = Math.min(hits.getLength() - searchQuery.getOffset(), searchQuery.getMaxItems());
                Hit[] show = hits.getHits(searchQuery.getOffset(), end);
                HitDetails[] details = bean.getDetails(show);
                Summary[] summaries = bean.getSummary(details, query);
                results.setNumberResults(hits.getLength());

                for (int i = 0; i < end; i++) {
                    SearchResultImpl searchResult = new SearchResultImpl();
                    populateSearchResult(searchResult, details[i], summaries[i]);
                    populateEndpointsLinks(searchResult);

                    searchResult.setFields(searchQuery.getFields());

                    if (showIds) {
                        searchResult.setId(show[i].getIndexDocNo());
                    }

                    searchResults.add(searchResult);
                }
            }
        } catch (IOException ex) {
            LOG.error("Exception performing the query", ex);
        }

        results.setResults(searchResults);
        return results;
    }

    private void populateSearchResult(SearchResultImpl searchResult, HitDetails detail, Summary summary) {
        searchResult.setTitle(detail.getValue("title"));
        searchResult.setOriginalURL(detail.getValue("url"));
        searchResult.setTstamp(Long.parseLong(this.parseTimeStamp(detail.getValue("tstamp").substring(0, 14))));
        searchResult.setContentLength(Long.parseLong(detail.getValue("contentLength")));
        searchResult.setDigest(detail.getValue("digest"));
        searchResult.setMimeType(detail.getValue("primaryType").concat("/").concat(detail.getValue("subType")));
        searchResult.setEncoding(detail.getValue("encoding"));
        searchResult.setDate(detail.getValue("date"));
        searchResult.setFileName(detail.getValue("arcname"));
        searchResult.setOffset(Long.parseLong(detail.getValue("arcoffset")));
        searchResult.setCollection(detail.getValue("collection"));
        searchResult.setSnippet(buildSnippet(summary));

        searchResult.setDetails(detail);
        searchResult.setBean(this.bean);
    }


    private void populateEndpointsLinks(SearchResultImpl searchResult) throws UnsupportedEncodingException {
        searchResult.setLinkToArchive(waybackServiceEndpoint +
                "/" + searchResult.getTstamp() +
                "/" + searchResult.getOriginalURL());

        searchResult.setLinkToNoFrame(waybackNoFrameServiceEndpoint +
                "/" + searchResult.getTstamp() +
                "/" + searchResult.getOriginalURL());

        searchResult.setLinkToScreenshot(screenshotServiceEndpoint +
                "?url=" + URLEncoder.encode(searchResult.getLinkToNoFrame(), StandardCharsets.UTF_8.toString()));

        searchResult.setLinkToExtractedText(extractedTextServiceEndpoint.concat("?m=")
                .concat(URLEncoder.encode(searchResult.getOriginalURL().concat("/").concat(searchResult.getTstamp()), StandardCharsets.UTF_8.toString())));

        searchResult.setLinkToMetadata(textSearchServiceEndpoint.concat("?metadata=")
                .concat(URLEncoder.encode(searchResult.getOriginalURL().concat("/").concat(searchResult.getTstamp()), StandardCharsets.UTF_8.toString())));

        searchResult.setLinkToOriginalFile(waybackNoFrameServiceEndpoint +
                "/" + searchResult.getTstamp() +
                "id_/" + searchResult.getOriginalURL());
    }

    private String parseTimeStamp(String tstamp) {
        try {
            Date d = this.dateFormat.parse(tstamp);
            return this.dateFormat.format(d);
        } catch (ParseException e) {
            LOG.error("Exception parsing a timestamp", e);
            return "";
        } catch (Exception e) {
            LOG.error("Unexpected exception", e);
            return "";
        }
    }

    private static String buildSnippet(Summary summary) {
        if (summary != null) {
            StringBuilder sum = new StringBuilder();
            Summary.Fragment[] fragments = summary.getFragments();
            for (int j = 0; j < fragments.length; j++) {
                if (fragments[j].isHighlight()) {
                    sum.append("<em>")
                            .append(Entities.encode(fragments[j].getText()))
                            .append("</em>");
                } else if (fragments[j].isEllipsis()) {
                    sum.append("<span class=\"ellipsis\"> ... </span>");
                } else {
                    sum.append(Entities.encode(fragments[j].getText()));
                }
            }
            return sum.toString();
        }
        return "";
    }

    private static String encodeVersionHistory(String versionHistory) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        StringBuilder sb = new StringBuilder(versionHistory.length());
        String[] urls = EntryPageExpansion.expandhttpAndhttps(versionHistory);
        for (int i = 0; i < urls.length; i++) {
            String encoded = Base32.encode(md.digest(urls[i].getBytes()));
            if (encoded != null && !encoded.equals(""))
                sb.append(" exacturl:").append(encoded);
        }
        return sb.toString();
    }

    public String getScreenshotServiceEndpoint() {
        return screenshotServiceEndpoint;
    }

    public String getWaybackServiceEndpoint() {
        return waybackServiceEndpoint;
    }

    public String getWaybackNoFrameServiceEndpoint() {
        return waybackNoFrameServiceEndpoint;
    }

    public String getExtractedTextServiceEndpoint() {
        return extractedTextServiceEndpoint;
    }

    public String getTextSearchServiceEndpoint() {
        return textSearchServiceEndpoint;
    }
}
