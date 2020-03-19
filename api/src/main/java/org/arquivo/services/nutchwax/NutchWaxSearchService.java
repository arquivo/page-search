package org.arquivo.services.nutchwax;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.lucene.search.PwaFunctionsWritable;
import org.apache.nutch.global.Global;
import org.apache.nutch.html.Entities;
import org.apache.nutch.searcher.*;
import org.archive.access.nutch.NutchwaxBean;
import org.archive.access.nutch.NutchwaxConfiguration;
import org.arquivo.services.SearchQuery;
import org.arquivo.services.SearchResult;
import org.arquivo.services.SearchResults;
import org.arquivo.services.SearchService;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class NutchWaxSearchService implements SearchService {

    private static final Log LOG = LogFactory.getLog(NutchWaxSearchService.class);

    private int numberResultsRanked;
    private Configuration conf;
    private NutchwaxBean bean;

    @Value("${searchpages.api.startdate}")
    private String startDate;

    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    @Value("${searchpages.service.link}")
    private String serviceName;

    @Value("${screenshot.service.endpoint}")
    private String screenshotServiceEndpoint;

    @Value("${wayback.service.endpoint}")
    private String waybackServiceEndpoint;

    @Value("${wayback.noframe.service.endpoint}")
    private String waybackNoFrameServiceEndpoint;

    @Value("${searchpages.extractedtext.service.link}")
    private String extractedTextServiceEndpoint;

    public NutchWaxSearchService() throws IOException {
        this.conf = NutchwaxConfiguration.getConfiguration();
        conf.addFinalResource("wax-default.xml");
        this.numberResultsRanked = Integer.parseInt(this.conf.get(Global.MAX_FULLTEXT_MATCHES_RANKED));
        this.bean = new NutchwaxBean(conf);
    }

    private boolean isTimeBoundedQuery(SearchQuery searchQuery) {
        return (searchQuery.getTo() != null || searchQuery.getFrom() != null);
    }

    @Override
    public SearchResults query(SearchQuery searchQuery) {
        SearchResults results = new SearchResults();
        ArrayList<SearchResult> searchResults = new ArrayList<>();

        StringBuilder queryString = new StringBuilder();

        String queryTerms = searchQuery.getQueryTerms();
        queryString.append(queryTerms);

        int limit = Integer.parseInt(searchQuery.getLimit());
        if (limit < 0) {
            limit = 0;
        }
        if (limit > numberResultsRanked) {
            limit = numberResultsRanked;
        }

        int hitsPerDup = Integer.parseInt(searchQuery.getLimitPerSite());

        // TODO handle this as a String array
        String siteParameter = searchQuery.getSite();
        if (siteParameter != null && !siteParameter.equals("")) {
            String[] siteParameters = siteParameter.split(",");
            if (siteParameters != null) {
                hitsPerDup = 0;

                for (String siteP : siteParameters) {
                    LOG.debug("siteP = " + siteP);
                    if (siteP.equals(""))
                        continue;

                    String site = "";
                    site = " site:".concat(siteP);
                    site = site.replaceAll("site:http://", "site:");
                    site = site.replaceAll("site:https://", "site:");
                    queryString.append(site);
                }
            }
        }

        // TODO handle this as a String array
        //Full-text search on specified type documents
        String typeParameter = searchQuery.getType();
        if (typeParameter == null)
            typeParameter = "";
        if (!typeParameter.equals("")) {
            String type = " type:".concat(typeParameter);
            queryString.append(type);
        }

        // TODO handle this as a String array
        //Full-text search on specified collections
        String collectionParameter = searchQuery.getCollection();
        if (collectionParameter != null && !collectionParameter.equals("")) {
            String[] collectionParameters = collectionParameter.split(",");
            if (collectionParameters != null) {
                for (String collection : collectionParameters) {
                    if (collection.equals(""))
                        continue;
                    String collections = "";
                    collections = " collection:".concat(collection);
                    LOG.debug("Collections Append: " + collections);
                    queryString.append(collections);
                }
            }
        }

        // Handle time bounded query
        // 'from' has a default. 'to' doesn't have a default
        // We want to do a timebounded query if 'to' and 'from' were specified on the query
        if (isTimeBoundedQuery(searchQuery)) {
            if (searchQuery.getFrom() == null) {
                searchQuery.setFrom(startDate);
            } else if (searchQuery.getFrom().length() != 14) {
                searchQuery.setFrom(StringUtils.rightPad(searchQuery.getFrom(), 14, "0"));
            }

            if (searchQuery.getTo() == null) {
                Date endDate = new Date();
                searchQuery.setTo(dateFormat.format(endDate));
            } else if (searchQuery.getTo().length() != 14) {
                searchQuery.setTo(StringUtils.rightPad(searchQuery.getTo(), 14, "0"));
            }
            queryString.append(" date:".concat(searchQuery.getFrom()).concat(searchQuery.getTo()));
        }

        try {
            Query query = Query.parse(queryString.toString(), conf);
            Hits hits = bean.search(query, limit, 2000,
                    hitsPerDup, "site", null, false,
                    PwaFunctionsWritable.parse(conf.get(Global.RANKING_FUNCTIONS)), 1);

            hits.setTotalIsExact(true);

            results.setNumberEstimatedResults(hits.getTotal());

            // build SearchResults
            if (hits.getLength() >= 1) {
                Hit[] show = hits.getHits(0, hits.getLength());
                HitDetails[] details = bean.getDetails(show);
                Summary[] summaries = bean.getSummary(details, query);
                results.setNumberResults(hits.getLength());

                for (int i = 0; i < hits.getLength(); i++) {
                    NutchWaxSearchResult searchResult = new NutchWaxSearchResult();
                    populateSearchResult(searchResult, details[i], summaries[i]);
                    populateEndpointsLinks(searchResult);

                    searchResult.setFields(((NutchWaxSearchQuery) searchQuery).getFields());
                    searchResults.add(searchResult);
                }
            }
        } catch (IOException ex) {
            LOG.error("Exception performing the query", ex);
        }

        results.setResults(searchResults);
        return results;
    }

    private void populateSearchResult(NutchWaxSearchResult searchResult, HitDetails detail, Summary summary) {
        searchResult.setTitle(detail.getValue("title"));
        searchResult.setOriginalURL(detail.getValue("url"));
        searchResult.setTimeStamp(Long.parseLong(this.parseTimeStamp(detail.getValue("tstamp").substring(0,14))));
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

    private void populateEndpointsLinks(NutchWaxSearchResult searchResult) {
        searchResult.setLinkToArchive(waybackServiceEndpoint +
                "/" + searchResult.getTimeStamp() +
                "/" + searchResult.getOriginalURL());

        searchResult.setLinkToScreenshot(screenshotServiceEndpoint +
                "?url=" + searchResult.getLinkToArchive());

        searchResult.setLinkToNoFrame(waybackNoFrameServiceEndpoint +
                "/" + searchResult.getTimeStamp() +
                "/" + searchResult.getOriginalURL());

        searchResult.setLinkToExtractedText(extractedTextServiceEndpoint +
                "?m=" + searchResult.getTimeStamp() +
                "/" + searchResult.getOriginalURL());
    }

    private String parseTimeStamp(String tstamp) {
        try {
            Date d = this.dateFormat.parse(tstamp);
            return this.dateFormat.format(d);
        } catch (ParseException e) {
            LOG.error("Exception parsing a timestamp", e);
            return "";
        }
    }

    private String buildSnippet(Summary summary) {
        if (summary != null) {
            StringBuffer sum = new StringBuffer();
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
}
