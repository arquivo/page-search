package pt.arquivo.services.nutchwax;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;
import org.springframework.beans.factory.annotation.Value;

import pt.arquivo.services.SearchQuery;
import pt.arquivo.services.SearchResult;
import pt.arquivo.services.SearchResultNutchImpl;
import pt.arquivo.services.SearchResults;
import pt.arquivo.services.SearchService;
import pt.arquivo.services.solr.SolrSearchService;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalNutchWaxSearchService implements SearchService {

    private static final Logger LOG = LoggerFactory.getLogger(SolrSearchService.class);

    @Value("${searchpages.textsearch.service.bean.fusion.nutchwax.textsearch.api.link:https://preprod.arquivo.pt/textsearch}")
    private String textSearchServiceEndpoint;

    public ExternalNutchWaxSearchService() {
        final Properties properties = new Properties();
        if (textSearchServiceEndpoint == null){
            try {
                LOG.info("Failed to autowire the textSearchServiceEndpoint, attempting to manually load properties...");
                properties.load(new FileInputStream("src/main/resources/application.properties"));
                textSearchServiceEndpoint = properties.getProperty("searchpages.textsearch.service.bean.fusion.nutchwax.textsearch.api.link");
            } catch (Exception e) {
                LOG.error("ExternalNutchWaxSearchService - Failed to open application.properties file: ", e);
                textSearchServiceEndpoint = "https://dev.arquivo.pt/textsearchnutchwax"; 
            }
        }
        
    }

    private URL searchQueryToUrl(SearchQuery searchQuery) throws MalformedURLException, UnsupportedEncodingException{
        StringBuilder stringBuilder = new StringBuilder();
        final String encoding = StandardCharsets.UTF_8.toString();
        stringBuilder.append(textSearchServiceEndpoint);
        stringBuilder.append("?q=").append(URLEncoder.encode(searchQuery.getQueryTerms(), encoding));
        if (searchQuery.getOffset() > 0){
            stringBuilder.append("&offset=").append(URLEncoder.encode(String.valueOf(searchQuery.getOffset()),encoding));
        }
        if (searchQuery.getMaxItems() != 50){
            stringBuilder.append("&maxItems=").append(URLEncoder.encode(String.valueOf(searchQuery.getMaxItems()), encoding));
        }
        if (searchQuery.getFrom() != null) {
            stringBuilder.append("&from=").append(URLEncoder.encode(searchQuery.getFrom(), encoding));
        }
        if (searchQuery.getTo() != null) {
            stringBuilder.append("&to=").append(URLEncoder.encode(searchQuery.getTo(), encoding));
        }
        if (searchQuery.getType() != null) {
            stringBuilder.append("&type=").append(URLEncoder.encode(String.join(",",searchQuery.getType()), encoding));
        }
        if (searchQuery.getSite() != null) {
            stringBuilder.append("&siteSearch=").append(URLEncoder.encode(String.join(",",searchQuery.getSite()), encoding));
        }
        if (searchQuery.getCollection() != null) {
            stringBuilder.append("&collection=").append(URLEncoder.encode(String.join(",",searchQuery.getCollection()), encoding));
        }
        if (searchQuery.getDedupField() != null) {
            stringBuilder.append("&dedupField=").append(URLEncoder.encode(searchQuery.getDedupField(), encoding));
        }
        if (searchQuery.getDedupValue() != 2) {
            stringBuilder.append("&dedupValue=").append(URLEncoder.encode(String.valueOf(searchQuery.getDedupValue()),encoding));
        }
        
        return new URL(stringBuilder.toString());
    }

    @Override
    public SearchResults query(SearchQuery searchQuery) {
        try {
            URL url = searchQueryToUrl(searchQuery);
            
            LOG.info("External Query: "+url.toString());
            return getExternalResults(url);
        } catch(Exception e) {
            
            LOG.error("External Query",e);
            return null;
        }
    }

    @Override
    public SearchResults query(SearchQuery searchQuery, boolean searchUrl) {
         if (searchUrl) {

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(textSearchServiceEndpoint);
            stringBuilder.append("?metadata=");
            stringBuilder.append(searchQuery.getQueryTerms());
            stringBuilder.append("/");
            stringBuilder.append(searchQuery.getFrom());
            try{
                URL url = new URL(stringBuilder.toString());
                
                LOG.info("External Query (urlQuery): "+url.toString());
                return getExternalResults(url);
            } catch(Exception e) {
                return null;
            }
            
        } else {    
            return query(searchQuery);
        }
    }

    private SearchResults getExternalResults (URL url){
        SearchResults searchResults = new SearchResults();
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            //Getting the response code
            int responsecode = conn.getResponseCode();

            if (responsecode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responsecode);
            } else {
            
                String inline = "";
                Scanner scanner = new Scanner(url.openStream());
            
            //Write all the JSON data into a string using a scanner
                while (scanner.hasNext()) {
                inline += scanner.nextLine();
                }
                
                //Close the scanner
                scanner.close();

                //Using the JSON simple library parse the string into a json object
                JSONParser parse = new JSONParser();
                JSONObject data_obj = (JSONObject) parse.parse(inline);

                JSONArray response_items = (JSONArray) data_obj.get("response_items");
                ArrayList<SearchResult> searchResultArrayList = new ArrayList<>();

                for (Object obj : response_items){
                    SearchResultNutchImpl searchResult = new SearchResultNutchImpl();
                    JSONObject json = (JSONObject) obj;

                    ArrayList<String> fields = new ArrayList<String> (json.keySet());
                    for (String field : fields){
                            switch (field) {
                                case "title":
                                    searchResult.setTitle((String) json.get(field));
                                    break;
                                case "originalURL":
                                    searchResult.setOriginalURL((String) json.get(field));
                                    break;
                                case "mimeType":
                                    searchResult.setMimeType((String) json.get(field));
                                    break;
                                case "tstamp":
                                    searchResult.setTstamp(Long.parseLong((String) json.get(field)));
                                    break;
                                case "digest":
                                    searchResult.setDigest((String) json.get(field));
                                    break;
                                case "collection":
                                    searchResult.setCollection((String) json.get(field));
                                    break;
                                case "id":
                                    searchResult.setId((String) json.get(field));
                                    break;
                                case "snippet":
                                    searchResult.setSnippet((String) json.get(field));
                                    break;
                                case "linkToArchive":
                                    searchResult.setLinkToArchive((String) json.get(field));
                                    break;
                                case "linkToNoFrame":
                                    searchResult.setLinkToNoFrame((String) json.get(field));
                                    break;
                                case "linkToScreenshot":
                                    searchResult.setLinkToScreenshot((String) json.get(field));
                                    break;
                                case "linkToExtractedText":
                                    searchResult.setLinkToExtractedText((String) json.get(field));
                                    break;
                                case "linkToMetadata":
                                    searchResult.setLinkToMetadata((String) json.get(field));
                                    break;
                                case "linkToOriginalFile":
                                    searchResult.setLinkToOriginalFile((String) json.get(field));
                                    break;
                
                            }
                    }

                    searchResultArrayList.add(searchResult);
                }

                searchResults.setResults(searchResultArrayList);
                searchResults.setEstimatedNumberResults((long) data_obj.get("estimated_nr_results"));
                searchResults.setLastPageResults(data_obj.get("next_page") == null);
            }
            
        } catch (Exception e) {
            LOG.error("External Query",e);
        }

        return searchResults;
    }
    
}
