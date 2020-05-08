package pt.arquivo.services;

import org.junit.Before;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchQueryImplTest {

    private SearchQueryImpl searchQuery;

    @Before
    public void setupSearchQuery() {
        searchQuery = new SearchQueryImpl("sapo ya", 0,
                10, 2, "20190101010101", "202001010101", new String[]{"text/html"},
                new String[]{"http://sapo.pt"}, new String[]{"FAWP"}, new String[]{"title", "collection"}, false);
    }

    @Test
    public void getQueryTerms() {
        assertEquals(searchQuery.getQueryTerms(), "sapo ya");
    }

    @Test
    public void setQueryTerms() {
        searchQuery.setQueryTerms("sapo");
        assertEquals(searchQuery.getQueryTerms(), "sapo");
    }

    @Test
    public void getStart() {
        assertEquals(searchQuery.getOffset(), 0);
    }

    @Test
    public void setStart() {
        searchQuery.setOffset(10);
        assertEquals(10, searchQuery.getOffset());
    }

    @Test
    public void getLimit() {
        assertEquals(10, searchQuery.getMaxItems());
    }

    @Test
    public void setLimit() {
        searchQuery.setMaxItems(50);
        assertEquals(50, searchQuery.getMaxItems());

        searchQuery.setMaxItems(-100);
        assertEquals(0, searchQuery.getMaxItems());
    }

    @Test
    public void getLimitPerSite() {
        assertEquals(2, searchQuery.getLimitPerSite());
    }

    @Test
    public void setLimitPerSite() {
        searchQuery.setLimitPerSite(4);
        assertEquals(4, searchQuery.getLimitPerSite());
    }

    @Test
    public void getFrom() {
        assertEquals("20190101010101", searchQuery.getFrom());
    }

    @Test
    public void setFrom() {
        searchQuery.setFrom("201901010101010101");
        assertEquals("20190101010101", searchQuery.getFrom());
    }

    @Test
    public void getTo() {
        assertEquals("202001010101", searchQuery.getTo());
    }

    @Test
    public void setTo() {
        searchQuery.setTo("2020010101010000");
        assertEquals("20200101010100", searchQuery.getTo());

        // should strip out extra characters
        searchQuery.setTo("202001010101010000000");
        assertEquals("20200101010101", searchQuery.getTo());
    }

    @Test
    public void getType() {
        assertEquals("text/html", searchQuery.getType()[0]);
    }

    @Test
    public void setType() {
        searchQuery.setType(new String[]{"application/pdf"});
        assertEquals(("application/pdf"), searchQuery.getType()[0]);
        assertTrue(searchQuery.isSearchByType());
    }

    @Test
    public void getSite() {
        assertEquals("http://sapo.pt", searchQuery.getSite()[0]);
    }

    @Test
    public void setSite() {
        searchQuery.setSite(new String[]{"http://arquivo.pt"});
        assertEquals("http://arquivo.pt", searchQuery.getSite()[0]);
        assertTrue(searchQuery.isSearchBySite());
    }

    @Test
    public void getCollection() {
        assertEquals("FAWP", searchQuery.getCollection()[0]);
    }

    @Test
    public void setCollection() {
        searchQuery.setCollection(new String[]{"CUSTOM"});
        assertEquals("CUSTOM", searchQuery.getCollection()[0]);
        assertTrue(searchQuery.isSearchByCollection());
    }

    @Test
    public void getPrettyPrint() {
        assertEquals(false, searchQuery.getPrettyPrint());
    }

    @Test
    public void setPrettyPrint() {
        searchQuery.setPrettyPrint(false);
        assertEquals(false, searchQuery.getPrettyPrint());
    }

    @Test
    public void getFields() {
        String[] fields = searchQuery.getFields();
        assertEquals("title", fields[0]);
        assertEquals("collection", fields[1]);
    }

    @Test
    public void setFields() {
        searchQuery.setFields(new String[]{"encoding", "type"});
    }
}
