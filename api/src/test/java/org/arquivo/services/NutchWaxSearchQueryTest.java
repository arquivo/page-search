package org.arquivo.services;

import org.arquivo.services.nutchwax.NutchWaxSearchQuery;
import org.junit.Before;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NutchWaxSearchQueryTest {

    private NutchWaxSearchQuery searchQuery;

    @Before
    public void setupSearchQuery(){
        searchQuery = new NutchWaxSearchQuery("sapo ya", 0,
                10, 2,"20190101010101", "202001010101", "text/html",
                new String[] { "http://sapo.pt" } , "FAWP", new String[]{"title", "collection"}, "false");
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
        assertEquals(searchQuery.getStart(),0);
    }

    @Test
    public void setStart() {
        searchQuery.setStart(10);
        assertEquals(searchQuery.getStart(), 10);
    }

    @Test
    public void getLimit() {
       assertEquals(searchQuery.getLimit(), 10);
    }

    @Test
    public void setLimit() {
        searchQuery.setLimit(50);
        assertEquals(searchQuery.getLimit(), 50);
    }

    @Test
    public void getLimitPerSite() {
        assertEquals(searchQuery.getLimitPerSite(), 2);
    }

    @Test
    public void setLimitPerSite() {
        searchQuery.setLimitPerSite(4);
        assertEquals(searchQuery.getLimitPerSite(), 4);
    }

    @Test
    public void getFrom() {
        assertEquals(searchQuery.getFrom(), "20190101010101");
    }

    @Test
    public void setFrom() {
        searchQuery.setFrom("201901010101010101");
        assertEquals("20190101010101", searchQuery.getFrom());
    }

    @Test
    public void getTo() {
        assertEquals(searchQuery.getTo(), "202001010101");
    }

    @Test
    public void setTo() {
        searchQuery.setTo("202001010101");
        assertEquals(searchQuery.getTo(),"202001010101");

        // should strip out extra characters
        searchQuery.setTo("20200101010101");
        assertEquals(searchQuery.getTo(), "20200101010101");
    }

    @Test
    public void getType() {
        assertEquals(searchQuery.getType(), "text/html");
    }

    @Test
    public void setType() {
        searchQuery.setType("application/pdf");
        assertEquals(searchQuery.getType(), ("application/pdf"));
    }

    @Test
    public void getSite() {
        assertEquals(searchQuery.getSite()[0], "http://sapo.pt");
    }

    @Test
    public void setSite() {
        searchQuery.setSite(new String[] {"http://arquivo.pt"} );
        assertEquals(searchQuery.getSite()[0], "http://arquivo.pt");
    }

    @Test
    public void getCollection() {
        assertEquals(searchQuery.getCollection(), "FAWP");
    }

    @Test
    public void setCollection() {
        searchQuery.setCollection("CUSTOM");
        assertEquals(searchQuery.getCollection(), "CUSTOM");
    }

    @Test
    public void getPrettyPrint() {
        assertEquals(searchQuery.getPrettyPrint(), "false");
    }

    @Test
    public void setPrettyPrint() {
        searchQuery.setPrettyPrint("false");
        assertEquals(searchQuery.getPrettyPrint(), "false");
    }

    @Test
    public void getFields() {
        String[] fields = searchQuery.getFields();
        assertEquals(fields[0], "title");
        assertEquals(fields[1], "collection");
    }

    @Test
    public void setFields() {
        searchQuery.setFields(new String[] {"encoding", "type"});
    }

    @Test public void testToString() {
        assertEquals( "TextSearchRequestParameters [queryTerms=sapo ya, offset=0, maxitems=10, " +
                "limitPerSite=2, from=" + "20190101010101" + ", to=202001010101" + ", type=text/html, site=http://sapo.pt," +
                " collection=FAWP, fields=title,collection, prettyPrint=false]", searchQuery.toString());
    }
}
