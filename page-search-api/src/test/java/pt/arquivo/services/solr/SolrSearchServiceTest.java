package pt.arquivo.services.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.util.NamedList;
import org.junit.Before;
import org.junit.Test;
import pt.arquivo.services.SearchQuery;
import pt.arquivo.services.SearchQueryImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SolrSearchServiceTest {

    private SolrSearchService service;

    @Before
    public void setUp() {
        service = new SolrSearchService();
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
    public void sanitizeDedupField_defaultsToTitleStringForNullOrInvalid() {
        assertThat(service.sanitizeDedupField(null)).isEqualTo("titleString");
        assertThat(service.sanitizeDedupField("")).isEqualTo("titleString");
        assertThat(service.sanitizeDedupField("not-a-real-field")).isEqualTo("titleString");
        // "title" is a valid dedup field name but is explicitly redirected to "titleString" too
        assertThat(service.sanitizeDedupField("title")).isEqualTo("titleString");
    }

    @Test
    public void sanitizeDedupField_mapsSiteAndSurtToSurtOldest() {
        assertThat(service.sanitizeDedupField("site")).isEqualTo("surtOldest");
        assertThat(service.sanitizeDedupField("surt")).isEqualTo("surtOldest");
        // matching is case-insensitive
        assertThat(service.sanitizeDedupField("SITE")).isEqualTo("surtOldest");
    }

    @Test
    public void sanitizeDedupField_mapsMimetypeToType() {
        assertThat(service.sanitizeDedupField("mimetype")).isEqualTo("type");
    }

    @Test
    public void sanitizeDedupField_passesThroughOtherValidFieldsUnchanged() {
        assertThat(service.sanitizeDedupField("type")).isEqualTo("type");
        assertThat(service.sanitizeDedupField("collection")).isEqualTo("collection");
    }

    @Test
    public void sanitizeQuery_preservesExactMatchQuotedPhrase() {
        SolrQuery solrQuery = new SolrQuery();
        String result = service.sanitizeQuery("\"hello world\"", solrQuery);
        // the phrase inside the quotes is still escaped like any other term (e.g. the space becomes "\ ")
        assertThat(result).isEqualTo("\"" + ClientUtils.escapeQueryChars("hello world") + "\"");
        assertThat(solrQuery.getFilterQueries()).isNull();
    }

    @Test
    public void sanitizeQuery_extractsExcludeTermsAsFilterQueries() {
        SolrQuery solrQuery = new SolrQuery();
        String result = service.sanitizeQuery("hello -world", solrQuery);
        assertThat(result).isEqualTo(ClientUtils.escapeQueryChars("hello "));
        assertThat(solrQuery.getFilterQueries()).containsExactlyInAnyOrder("-content:world", "-title:world");
    }

    @Test
    public void sanitizeQuery_escapesSpecialCharacters() {
        SolrQuery solrQuery = new SolrQuery();
        String result = service.sanitizeQuery("hello (world)", solrQuery);
        assertThat(result).isEqualTo(ClientUtils.escapeQueryChars("hello (world)"));
    }

    @Test
    public void convertSearchQuery_defaultsToMatchAllWhenNoQueryTerms() {
        SearchQueryImpl searchQuery = new SearchQueryImpl(null);
        SolrQuery solrQuery = service.convertSearchQuery(searchQuery);
        assertThat(solrQuery.getQuery()).isEqualTo("*:*");
    }

    @Test
    public void convertSearchQuery_mapsPdfTypeToApplicationPdfMimetype() {
        SearchQueryImpl searchQuery = new SearchQueryImpl("sapo");
        searchQuery.setType(new String[] { "pdf" });
        SolrQuery solrQuery = service.convertSearchQuery(searchQuery);
        assertThat(solrQuery.getFilterQueries())
                .contains("type:" + ClientUtils.escapeQueryChars("application/pdf"));
    }

    @Test
    public void convertSearchQuery_mapsOfficeAliasTypeToTwoMimetypesWithOr() {
        SearchQueryImpl searchQuery = new SearchQueryImpl("sapo");
        searchQuery.setType(new String[] { "xls" });
        SolrQuery solrQuery = service.convertSearchQuery(searchQuery);
        String expected = "type:" + ClientUtils.escapeQueryChars("application/vnd.ms-excel")
                + " OR type:" + ClientUtils.escapeQueryChars("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        assertThat(solrQuery.getFilterQueries()).contains(expected);
    }

    @Test
    public void convertSearchQuery_unknownTypeFallsBackToWildcardMatch() {
        SearchQueryImpl searchQuery = new SearchQueryImpl("sapo");
        searchQuery.setType(new String[] { "zzz" });
        SolrQuery solrQuery = service.convertSearchQuery(searchQuery);
        assertThat(solrQuery.getFilterQueries())
                .contains("type:*" + ClientUtils.escapeQueryChars("zzz") + "*");
    }

    @Test
    public void convertSearchQuery_explicitMimetypeWithSlashIsUsedAsIs() {
        SearchQueryImpl searchQuery = new SearchQueryImpl("sapo");
        searchQuery.setType(new String[] { "application/zip" });
        SolrQuery solrQuery = service.convertSearchQuery(searchQuery);
        assertThat(solrQuery.getFilterQueries())
                .contains("type:" + ClientUtils.escapeQueryChars("application/zip"));
    }

    @Test
    public void timestampSurtTo_extractsCollectionTimestampAndSurt() {
        String urlTimestamp = "COLLECTION1/20190101000000/(com,example,)/path";
        assertThat(service.timestampSurtToCollection(urlTimestamp)).isEqualTo("COLLECTION1");
        assertThat(service.timestampSurtToTimestamp(urlTimestamp)).isEqualTo("20190101000000");
        assertThat(service.timestampSurtToSurt(urlTimestamp)).isEqualTo("(com,example,)/path");
    }

    @Test
    public void filterUrlTimestamps_dropsEntriesWithoutSlash() {
        List<Object> urlstimestamps = new ArrayList<>(Arrays.asList("malformed-entry-no-slash"));
        List<Object> result = service.filterUrlTimestamps(urlstimestamps, null, null, null, null);
        assertThat(result).isEmpty();
    }

    @Test
    public void filterUrlTimestamps_filtersBySiteSearchSurtPrefix() {
        List<Object> urlstimestamps = new ArrayList<>(Arrays.asList(
                "COLLECTION1/20190101000000/(com,example,)/path1",
                "COLLECTION1/20190101000000/(com,other,)/path2"));
        List<Object> result = service.filterUrlTimestamps(urlstimestamps, null, null,
                new String[] { "(com,example,)" }, null);
        assertThat(result).containsExactly("COLLECTION1/20190101000000/(com,example,)/path1");
    }

    @Test
    public void filterUrlTimestamps_filtersByTimeRange() {
        List<Object> urlstimestamps = new ArrayList<>(Arrays.asList(
                "COLLECTION1/20180101000000/(com,example,)/path1",
                "COLLECTION1/20190101000000/(com,example,)/path2",
                "COLLECTION1/20200101000000/(com,example,)/path3"));
        List<Object> result = service.filterUrlTimestamps(urlstimestamps, 20191231000000L, 20190101000000L, null, null);
        assertThat(result).containsExactly("COLLECTION1/20190101000000/(com,example,)/path2");
    }

    @Test
    public void filterUrlTimestamps_filtersByCollection() {
        List<Object> urlstimestamps = new ArrayList<>(Arrays.asList(
                "COLLECTION1/20190101000000/(com,example,)/path1",
                "COLLECTION2/20190101000000/(com,example,)/path2"));
        List<Object> result = service.filterUrlTimestamps(urlstimestamps, null, null, null,
                new String[] { "COLLECTION1" });
        assertThat(result).containsExactly("COLLECTION1/20190101000000/(com,example,)/path1");
    }

    @Test
    public void getOldestUrlTimestamp_returnsEntryWithSmallestTimestamp() {
        List<Object> urlstimestamps = new ArrayList<>(Arrays.asList(
                "COLLECTION1/20200101000000/(com,example,)/path1",
                "COLLECTION1/20180101000000/(com,example,)/path2",
                "COLLECTION1/20190101000000/(com,example,)/path3"));
        assertThat(service.getOldestUrlTimestamp(urlstimestamps))
                .isEqualTo("COLLECTION1/20180101000000/(com,example,)/path2");
    }

    private static QueryResponse queryResponseWithCollation(String collation) {
        NamedList<Object> collations = new NamedList<>();
        if (collation != null) {
            collations.add("collation", collation);
        }
        NamedList<Object> spellcheck = new NamedList<>();
        // SpellCheckResponse bails out before reading "collations" unless "suggestions" is present too
        spellcheck.add("suggestions", new NamedList<>());
        spellcheck.add("collations", collations);
        NamedList<Object> response = new NamedList<>();
        response.add("spellcheck", spellcheck);

        QueryResponse queryResponse = new QueryResponse();
        queryResponse.setResponse(response);
        return queryResponse;
    }

    @Test
    public void parseSuggestedQuery_returnsNullWhenNoSpellCheckResponse() {
        QueryResponse queryResponse = new QueryResponse();
        queryResponse.setResponse(new NamedList<>());
        SearchQueryImpl searchQuery = new SearchQueryImpl("sapo");
        assertThat(service.parseSuggestedQuery(queryResponse, searchQuery)).isNull();
    }

    @Test
    public void parseSuggestedQuery_returnsNullWhenCollationMatchesOriginalQuery() {
        SearchQueryImpl searchQuery = new SearchQueryImpl("hello");
        QueryResponse queryResponse = queryResponseWithCollation("\"hello\"");
        assertThat(service.parseSuggestedQuery(queryResponse, searchQuery)).isNull();
    }

    @Test
    public void parseSuggestedQuery_stripsQuotesWeAddedOurselves() {
        SearchQueryImpl searchQuery = new SearchQueryImpl("origanal query");
        QueryResponse queryResponse = queryResponseWithCollation("\"original query\"");
        assertThat(service.parseSuggestedQuery(queryResponse, searchQuery)).isEqualTo("original query");
    }

    @Test
    public void parseSuggestedQuery_doesNotStripQuotesUserAlreadyProvided() {
        // queryTerms is already quoted by the user, so getQuotedQueryTerms() == queryTerms and no quotes were added by us
        SearchQueryImpl searchQuery = new SearchQueryImpl("\"origanal query\"");
        QueryResponse queryResponse = queryResponseWithCollation("\"original query\"");
        assertThat(service.parseSuggestedQuery(queryResponse, searchQuery)).isEqualTo("\"original query\"");
    }
}