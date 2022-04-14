package pt.arquivo.services;

import org.json.*;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.List;

public class SearchResultSerializer extends JsonSerializer {

    private static final Logger LOG = LoggerFactory.getLogger(SearchResultSerializer.class);

    @Value("${searchpages.api.show.ids}")
    private boolean showIds;

    @Override
    public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        SearchResult searchResult = (SearchResult) o;
        jsonGenerator.writeStartObject();
        if (searchResult.getFields() != null) {
            for (Field field : searchResult.getClass().getDeclaredFields()) {
                if (serializeField(field.getName(), searchResult.getFields())) {
                    try {
                        field.setAccessible(true);
                        if (field.get(searchResult) != null)
                            jsonGenerator.writeObjectField(field.getName(), field.get(searchResult));
                    } catch (IllegalAccessException e) {
                        LOG.error("Error trying to access field", e);
                    }
                }
            }
        } else {
            for (Field field : searchResult.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(searchResult);
                    if (value != null) {
                        if (field.getName().equals("id")) {
                            if (showIds) {
                                jsonGenerator.writeObjectField(field.getName(), field.get(searchResult));
                            }
                        } else if (field.getName().equals("linkToOriginalFile")) {
                            jsonGenerator.writeObjectField(field.getName(), field.get(searchResult));
                            JSONObject extractions = new JSONObject(runPython(field.get(searchResult).toString()));
                            jsonGenerator.writeObjectField("parsedInformation", extractions);
                        } else if (!field.getName().equals("LOG") && !field.getName().equals("bean")
                                && !field.getName().equals("details") && !field.getName().equals("fields")
                                && !field.getName().equals("solrClient")) {
                            jsonGenerator.writeObjectField(field.getName(), field.get(searchResult));
                        }
                    }
                } catch (IllegalAccessException e) {
                    LOG.error("Error trying to access field", e);
                }
            }
        }
        jsonGenerator.writeEndObject();
    }

    private boolean serializeField(String fieldName, String[] fields) {
        if (fields != null) {
            for (String field : fields) {
                if (fieldName.equalsIgnoreCase(field))
                    return true;
            }
            return false;
        } else {
            return true;
        }
    }

    private String runPython( String argument) {

        String basePath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        ProcessBuilder processBuilder = new ProcessBuilder("python3.9", basePath+"/teste.py", argument);
        processBuilder.redirectErrorStream(true);
    
        try {

        
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        
        String line, output;
        output = "";
        while ((line = reader.readLine()) != null) {
            output += line;
        }



        int exitCode = process.waitFor();
        return output;

        } catch (Exception e) {
            return "Failed :(";
        }
    }
}
