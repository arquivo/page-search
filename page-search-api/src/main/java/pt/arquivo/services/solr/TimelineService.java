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

import pt.arquivo.services.Timeline;

/**
 * Computes the timeline of a query: how many documents match on each year of the archive, and how big that is when
 * compared to everything the archive holds for that year.
 *
 * <p>The counts come from a Solr range facet over dateOldest, the year a document was first archived. The totals per
 * year are the same facet over the whole index: they only change when the index changes, so they are cached instead of
 * being computed on every query.
 */
public class TimelineService {

    private static final Logger LOG = LoggerFactory.getLogger(TimelineService.class);

    /** The field holding the moment a document was first archived. It has docValues, so faceting over it is cheap. */
    static final String TIMELINE_FIELD = "dateOldest";

    private final SolrClient solrClient;
    private final int startYear;
    private final long baselineTtlMillis;

    private Map<String, Long> totalsPerYear;
    private long totalsPerYearFetchedAt;

    public TimelineService(SolrClient solrClient, int startYear, long baselineTtlMillis) {
        this.solrClient = solrClient;
        this.startYear = startYear;
        this.baselineTtlMillis = baselineTtlMillis;
    }

    /**
     * The last year the timeline covers. Documents are archived every year, so it follows the clock.
     */
    private int endYear() {
        return Year.now().getValue();
    }

    /**
     * Asks Solr for the number of matching documents per year. Every year of the archive is asked for, including the
     * ones without matches (facet.mincount=0), so the reply always has the full timeline.
     *
     * @param solrQuery the query to facet, it is modified in place
     */
    void addYearRangeFacet(SolrQuery solrQuery) {
        solrQuery.setFacet(true);
        solrQuery.set("facet.range", TIMELINE_FIELD);
        solrQuery.set("facet.range.start", solrYear(startYear));
        solrQuery.set("facet.range.end", solrYear(endYear() + 1));
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
            if (!TIMELINE_FIELD.equals(rangeFacet.getName())) {
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
     * Builds the timeline of a query out of its faceted reply, normalizing the counts by the size of the archive on
     * each year.
     */
    Timeline buildTimeline(QueryResponse queryResponse) {
        return Timeline.of(parseYearlyCounts(queryResponse), getTotalsPerYear());
    }

    /**
     * How many documents the whole archive has per year. Cached, it is a full index facet and it only moves when
     * documents are indexed. On failure the previous value is kept (an empty one on the first try), which only means
     * the impact is reported as 0.
     */
    synchronized Map<String, Long> getTotalsPerYear() {
        boolean expired = System.currentTimeMillis() - totalsPerYearFetchedAt > baselineTtlMillis;
        if (totalsPerYear != null && !expired) {
            return totalsPerYear;
        }

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("*:*");
        solrQuery.setRows(0);
        solrQuery.set("hl", "false");
        solrQuery.set("spellcheck", "false");
        addYearRangeFacet(solrQuery);

        LOG.info("Solr Query (timeline baseline): " + solrQuery);
        try {
            totalsPerYear = Collections.unmodifiableMap(parseYearlyCounts(solrClient.query(solrQuery)));
            totalsPerYearFetchedAt = System.currentTimeMillis();
        } catch (SolrServerException | IOException e) {
            LOG.error("Error querying Solr for the timeline baseline: ", e);
            if (totalsPerYear == null) {
                totalsPerYear = Collections.emptyMap();
            }
        }
        return totalsPerYear;
    }
}
