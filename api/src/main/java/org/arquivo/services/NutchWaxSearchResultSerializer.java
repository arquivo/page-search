package org.arquivo.services;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.lang.reflect.Field;


// TODO ughhhh should try to do this in other way
public class NutchWaxSearchResultSerializer extends JsonSerializer {

    @Override
    public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        NutchWaxSearchResult searchResult = (NutchWaxSearchResult) o;
        jsonGenerator.writeStartObject();
        if (searchResult.getFields() != null) {
            for (Field field : searchResult.getClass().getDeclaredFields()){
                if (serializeField(field.getName(), searchResult.getFields())){
                    try {
                        field.setAccessible(true);
                        jsonGenerator.writeObjectField(field.getName(), field.get(searchResult));
                    } catch (IllegalAccessException e) {
                        // TODO handle this
                        e.printStackTrace();
                    };
                }
            }
        }
        else {
            for (Field field : searchResult.getClass().getDeclaredFields()){
                field.setAccessible(true);
                try {
                    if (field.getName() != "bean" && field.getName() != "details" && field.getName() != "fields"){
                        jsonGenerator.writeObjectField(field.getName(), field.get(searchResult));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
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

