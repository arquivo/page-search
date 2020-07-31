package pt.arquivo.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import pt.arquivo.services.SearchResult;

import java.util.ArrayList;

@ApiModel
public class MetadataResponse implements ApiResponse {
   private String serviceName;
   private String linkToService;

   @JsonProperty("response_items")
   private ArrayList<SearchResult> responseItems;

   public String getServiceName() {
      return serviceName;
   }

   public void setServiceName(String serviceName) {
      this.serviceName = serviceName;
   }

   public String getLinkToService() {
      return linkToService;
   }

   public void setLinkToService(String linkToService) {
      this.linkToService = linkToService;
   }

   public ArrayList<SearchResult> getResponseItems() {
      return responseItems;
   }

   public void setResponseItems(ArrayList<SearchResult> responseItems) {
      this.responseItems = responseItems;
   }
}
