package pt.arquivo.services.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Test;
import pt.arquivo.services.SearchQuery;
import pt.arquivo.services.SearchQueryImpl;
import pt.arquivo.services.SearchServiceConfiguration;

import java.time.Year;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SolrSearchServiceTest {

    private final SolrSearchService solrSearchService = solrSearchService();

    /**
     * A service that isn't wired by Spring, so it needs the configuration the timeline reads (the first year of the
     * archive) to be given to it. No query is made against the Solr URL by these tests.
     */
    private static SolrSearchService solrSearchService() {
        SearchServiceConfiguration configuration = new SearchServiceConfiguration();
        configuration.setStartDate("19960101000000");
        configuration.setBaseSolrUrl("http://localhost:8983/solr/pages");
        SolrSearchService solrSearchService = new SolrSearchService(configuration);

        // An archive starting on 1996 and holding ten times more documents on 2008 than on 1997, so that the volumes
        // are known without a Solr to ask
        Map<String, Long> volumesPerYear = new LinkedHashMap<>();
        volumesPerYear.put("1996", 50000L);
        volumesPerYear.put("1997", 100000L);
        volumesPerYear.put("2008", 1000000L);
        solrSearchService.setYearVolumes(new YearVolumes(volumesPerYear));

        return solrSearchService;
    }

    private static SearchQuery timelineQuery() {
        SearchQuery searchQuery = new SearchQueryImpl("eleições");
        searchQuery.setTimeline(true);
        return searchQuery;
    }

    @Test
    public void isLastPage() {
        SearchQuery searchQuery = new SearchQueryImpl("sapo");
        searchQuery.setOffset(10);
        searchQuery.setMaxItems(10);
        assertThat(SolrSearchService.isLastPage(20, searchQuery)).isTrue();
        assertThat(SolrSearchService.isLastPage(21, searchQuery)).isFalse();
    }

    @Test
    public void timelineFacetsByYear() {
        SolrQuery timelineQuery = solrSearchService.convertTimelineQuery(timelineQuery());

        assertThat(timelineQuery.getBool("facet")).isTrue();
        assertThat(timelineQuery.get("facet.range")).isEqualTo("dateOldest");
        assertThat(timelineQuery.get("facet.range.gap")).isEqualTo("+1YEAR");
        // The whole archive is covered, from its first year up to the one running now
        assertThat(timelineQuery.get("facet.range.start")).isEqualTo("1996-01-01T00:00:00Z");
        assertThat(timelineQuery.get("facet.range.end"))
                .isEqualTo((Year.now().getValue() + 1) + "-01-01T00:00:00Z");
        // Years without matches still come back, the timeline is meant to show the gaps
        assertThat(timelineQuery.get("facet.mincount")).isEqualTo("0");
    }

    @Test
    public void timelineQueryAsksForNoDocuments() {
        SearchQuery searchQuery = timelineQuery();
        searchQuery.setOffset(50);
        searchQuery.setMaxItems(50);

        SolrQuery timelineQuery = solrSearchService.convertTimelineQuery(searchQuery);

        assertThat(timelineQuery.getRows()).isEqualTo(0);
        assertThat(timelineQuery.getStart()).isEqualTo(0);
        assertThat(timelineQuery.get("hl")).isEqualTo("false");
        assertThat(timelineQuery.get("spellcheck")).isEqualTo("false");
    }

    @Test
    public void timelineQueryIsNotDeduplicated() {
        SearchQuery searchQuery = timelineQuery();

        // The search collapses on the dedup field, which is a costly post filter and would leave the yearly counts no
        // longer comparable with the counts of the whole archive
        assertThat(solrSearchService.convertSearchQuery(searchQuery).getFilterQueries())
                .anyMatch(filterQuery -> filterQuery.startsWith("{!collapse"));

        SolrQuery timelineQuery = solrSearchService.convertTimelineQuery(searchQuery);
        assertThat(timelineQuery.getFilterQueries()).isNullOrEmpty();
        assertThat(timelineQuery.get("expand")).isNull();
    }

    @Test
    public void timelineQueryKeepsTheSearchFilters() {
        SearchQuery searchQuery = timelineQuery();
        searchQuery.setType(new String[]{"pdf"});
        searchQuery.setCollection(new String[]{"AWP1"});

        SolrQuery timelineQuery = solrSearchService.convertTimelineQuery(searchQuery);

        assertThat(timelineQuery.getQuery()).isEqualTo("eleições");
        assertThat(timelineQuery.getFilterQueries())
                .contains("type:application\\/pdf", "collections:AWP1")
                .noneMatch(filterQuery -> filterQuery.startsWith("{!collapse"));
    }

    @Test
    public void yearBalanceBoostsTheThinYears() {
        SearchQuery searchQuery = new SearchQueryImpl("eleições");
        searchQuery.setYearBalance(1.0);

        SolrQuery solrQuery = solrSearchService.convertSearchQuery(searchQuery);

        // 1996 is the thinnest year of this archive, so at full strength it is lifted by MAX_LIFT
        assertThat(solrQuery.get("boost")).contains("ms(dateOldest)").contains("1.2500");
    }

    @Test
    public void searchQueryIsNotBoostedWhenYearBalanceIsntAskedFor() {
        assertThat(solrSearchService.convertSearchQuery(new SearchQueryImpl("eleições")).get("boost")).isNull();
    }

    @Test
    public void timelineQueryIsNotBoosted() {
        SearchQuery searchQuery = timelineQuery();
        searchQuery.setYearBalance(1.0);

        // Counting the documents of each year has no use for how they are scored
        assertThat(solrSearchService.convertTimelineQuery(searchQuery).get("boost")).isNull();
    }

    @Test
    public void searchQueryIsNotFacetedWhenTheTimelineIsntAskedFor() {
        SolrQuery solrQuery = solrSearchService.convertSearchQuery(new SearchQueryImpl("eleições"));

        assertThat(solrQuery.getBool("facet", false)).isFalse();
        assertThat(solrQuery.get("facet.range")).isNull();
    }
}
