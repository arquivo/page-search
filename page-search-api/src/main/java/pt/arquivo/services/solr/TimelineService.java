package pt.arquivo.services.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

import pt.arquivo.services.Timeline;

/**
 * Computes the timeline of a query: how many documents match on each year of the archive, and how big that is when
 * compared to everything the archive holds for that year.
 *
 * <p>The counts come from a Solr range facet over the year field. The totals that normalize them are the volumes of
 * the archive itself, which are shared with the year balance ranking, see {@link YearVolumes}.
 */
public class TimelineService {

    private final YearVolumes yearVolumes;

    public TimelineService(YearVolumes yearVolumes) {
        this.yearVolumes = yearVolumes;
    }

    /**
     * Asks Solr for the number of matching documents per year.
     *
     * @param solrQuery the query to facet, it is modified in place
     */
    void addYearRangeFacet(SolrQuery solrQuery) {
        yearVolumes.addYearRangeFacet(solrQuery);
    }

    /**
     * Builds the timeline of a query out of its faceted reply, normalizing the counts by the size of the archive on
     * each year.
     */
    Timeline buildTimeline(QueryResponse queryResponse) {
        return Timeline.of(YearVolumes.parseYearlyCounts(queryResponse), yearVolumes.perYear());
    }
}
