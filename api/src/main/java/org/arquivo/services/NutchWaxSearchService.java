package org.arquivo.services;

import org.apache.hadoop.conf.Configuration;
import org.apache.lucene.search.PwaFunctionsWritable;
import org.apache.nutch.global.Global;
import org.apache.nutch.searcher.*;
import org.archive.access.nutch.NutchwaxBean;
import org.archive.access.nutch.NutchwaxConfiguration;

import java.io.IOException;
import java.util.ArrayList;

public class NutchWaxSearchService implements SearchService {

    @Override
    public ArrayList<SearchResult> query(SearchQuery searchQuery) {
        ArrayList<SearchResult> searchResults = new ArrayList<>();

        // nutchwax service
        // load the configuration (nucthwax-defaults.xml)
        // import class from hadoop 0.14.... lets see if we dont have problems with it
        Configuration conf = NutchwaxConfiguration.getConfiguration();
        conf.addFinalResource("wax-default.xml");

        NutchwaxBean bean = null;

        try {
            bean = new NutchwaxBean(conf);

            // build query string from searchQuery
            StringBuilder queryString = new StringBuilder();

            String queryTerms = searchQuery.getQueryTerms();
            queryString.append(queryTerms);

            Query query = Query.parse(queryString.toString(), conf);
            Hits hits = bean.search(query, 10, 2000, 2, "site", null, false, PwaFunctionsWritable.parse(conf.get(Global.RANKING_FUNCTIONS)), 1);
            hits.setTotalIsExact(true);

            // build SearchResults
            if (hits.getLength() >= 1) {
                Hit[] show = hits.getHits(0, hits.getLength());
                HitDetails[] details = bean.getDetails(show);
                Summary[] summaries = bean.getSummary(details, query);

                // better iterate?
                for (int i = 0; i < hits.getLength(); i++) {
                    SearchResult searchResult = new SearchResult();
                    searchResult.setTitle(details[i].getValue("title"));
                    searchResult.setOriginalURL(details[i].getValue("url"));
                    searchResult.setTimeStamp(Long.parseLong(details[i].getValue("tstamp")));
                    searchResults.add(searchResult);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResults;
    }
}
