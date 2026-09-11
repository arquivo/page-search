package pt.arquivo.services.solr;

import java.io.IOException;
import java.time.Year;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * How many documents the archive holds per year.
 *
 * <p>This is the same number for every query and it only moves when indexing does, so it is fetched once and reused.
 * Two features are built on top of it: the timeline normalizes the counts of a query by it, and the year balance
 * ranking uses it to tell the thin years of the archive from the dense ones.
 */
public class YearVolumes {

    private static final Logger LOG = LoggerFactory.getLogger(YearVolumes.class);

    /** The field holding the moment a document was first archived. It has docValues, so faceting over it is cheap. */
    static final String YEAR_FIELD = "dateOldest";

    private final SolrClient solrClient;
    private int startYear;
    private final long ttlMillis;

    private Map<String, Long> perYear;
    private long fetchedAt;

    public YearVolumes(SolrClient solrClient, int startYear, long ttlMillis) {
        this.solrClient = solrClient;
        this.startYear = startYear;
        this.ttlMillis = ttlMillis;
    }

    /**
     * The volumes of an archive that isn't there to be asked, for the tests that need them fixed.
     */
    YearVolumes(Map<String, Long> perYear) {
        this(null, 0, Long.MAX_VALUE);
        this.perYear = Collections.unmodifiableMap(new LinkedHashMap<>(perYear));
        this.fetchedAt = System.currentTimeMillis();
        // An archive that starts on the first year it holds documents for
        this.startYear = perYear.keySet().stream().mapToInt(Integer::parseInt).min().orElse(Year.now().getValue());
    }

    int getStartYear() {
        return startYear;
    }

    /**
     * The last year the archive covers. Documents are archived every year, so it follows the clock.
     */
    int getEndYear() {
        return Year.now().getValue();
    }

    /**
     * Asks Solr for the number of documents per year. Every year of the archive is asked for, including the ones
     * without documents (facet.mincount=0), so the reply always covers the full range.
     *
     * @param solrQuery the query to facet, it is modified in place
     */
    void addYearRangeFacet(SolrQuery solrQuery) {
        solrQuery.setFacet(true);
        solrQuery.set("facet.range", YEAR_FIELD);
        solrQuery.set("facet.range.start", solrYear(getStartYear()));
        solrQuery.set("facet.range.end", solrYear(getEndYear() + 1));
        solrQuery.set("facet.range.gap", "+1YEAR");
        solrQuery.set("facet.mincount", "0");
    }

    private static String solrYear(int year) {
        return year + "-01-01T00:00:00Z";
    }

    /**
     * Reads the yearly counts out of a range facet reply, keyed by year.
     *
     * @param queryResponse a reply to a query built with {@link #addYearRangeFacet}
     * @return the counts per year, empty when the reply has no range facet
     */
    static Map<String, Long> parseYearlyCounts(QueryResponse queryResponse) {
        Map<String, Long> counts = new LinkedHashMap<>();
        List<RangeFacet> rangeFacets = queryResponse.getFacetRanges();
        if (rangeFacets == null) {
            return counts;
        }
        for (RangeFacet<?, ?> rangeFacet : rangeFacets) {
            if (!YEAR_FIELD.equals(rangeFacet.getName())) {
                continue;
            }
            for (RangeFacet.Count count : rangeFacet.getCounts()) {
                // The facet value is the start of the bucket, e.g. 2005-01-01T00:00:00Z
                counts.put(count.getValue().substring(0, 4), (long) count.getCount());
            }
        }
        return counts;
    }

    /**
     * How many documents the whole archive has per year, from the cache when it is still fresh. On failure the
     * previous value is kept (an empty one on the first try), which leaves the features built on it neutral instead of
     * failing the search.
     */
    public synchronized Map<String, Long> perYear() {
        boolean expired = System.currentTimeMillis() - fetchedAt > ttlMillis;
        if (perYear != null && !expired) {
            return perYear;
        }

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("*:*");
        solrQuery.setRows(0);
        solrQuery.set("hl", "false");
        solrQuery.set("spellcheck", "false");
        addYearRangeFacet(solrQuery);

        LOG.info("Solr Query (year volumes): " + solrQuery);
        try {
            perYear = Collections.unmodifiableMap(parseYearlyCounts(solrClient.query(solrQuery)));
            fetchedAt = System.currentTimeMillis();
        } catch (SolrServerException | IOException e) {
            LOG.error("Error querying Solr for the volumes per year: ", e);
            if (perYear == null) {
                perYear = Collections.emptyMap();
            }
        }
        return perYear;
    }
}
