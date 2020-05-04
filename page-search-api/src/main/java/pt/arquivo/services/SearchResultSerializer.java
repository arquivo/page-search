package pt.arquivo.services;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.Field;

public class SearchResultSerializer extends JsonSerializer {

    private static final Log LOG = LogFactory.getLog(SearchResultSerializer.class);

    @Override
    public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        SearchResultImpl searchResult = (SearchResultImpl) o;
        jsonGenerator.writeStartObject();
        if (searchResult.getFields() != null) {
            for (Field field : searchResult.getClass().getDeclaredFields()){
                if (serializeField(field.getName(), searchResult.getFields())){
                    try {
                        field.setAccessible(true);
                        jsonGenerator.writeObjectField(field.getName(), field.get(searchResult));
                    } catch (IllegalAccessException e) {
                        LOG.error("Error trying to access field", e);
                    }
                }
            }
        }
        else {
            for (Field field : searchResult.getClass().getDeclaredFields()){
                field.setAccessible(true);
                try {
                    if (field.getName().equals("statusCode")){
                        if ((Integer) field.get(searchResult) != 0)
                           jsonGenerator.writeObjectField(field.getName(), field.get(searchResult));
                    }
                    else if (!field.getName().equals("LOG") && !field.getName().equals("bean")
                            && !field.getName().equals("details") && !field.getName().equals("fields")){
                        jsonGenerator.writeObjectField(field.getName(), field.get(searchResult));
                    }
                } catch (IllegalAccessException e) {
                    LOG.error("Error trying to access field", e);
                }
            }
        }
        jsonGenerator.writeEndObject();
    }

    private boolean serializeField(String fieldName, String[] fields) {
        if (fields != null){
            for (String field : fields) {
                if (fieldName.equalsIgnoreCase(field)) return true;
            }
            return false;
        }
        else {
            return true;
        }
    }
}
