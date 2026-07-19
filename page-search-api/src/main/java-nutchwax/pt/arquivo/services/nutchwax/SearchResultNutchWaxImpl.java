package pt.arquivo.services.nutchwax;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.nutch.searcher.HitDetails;
import org.apache.nutch.searcher.NutchBean;
import pt.arquivo.services.SearchResultNutchImpl;

import java.io.IOException;

public class SearchResultNutchWaxImpl extends SearchResultNutchImpl {

    private static final Log LOG = LogFactory.getLog(SearchResultNutchWaxImpl.class);

    private NutchBean bean;
    private HitDetails details;

    @JsonIgnore
    @Override
    public String getExtractedText() {
        try {
            return this.bean.getParseText(this.details).getText();
        } catch (IOException e) {
            LOG.error("Error while extracting text: ", e);
        }
        return "";
    }

    public HitDetails getDetails() {
        return details;
    }

    public void setDetails(HitDetails details) {
        this.details = details;
    }

    public NutchBean getBean() {
        return bean;
    }

    public void setBean(NutchBean bean) {
        this.bean = bean;
    }
}
