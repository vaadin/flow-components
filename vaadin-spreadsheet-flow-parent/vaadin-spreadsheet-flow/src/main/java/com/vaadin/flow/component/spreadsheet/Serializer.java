package com.vaadin.flow.component.spreadsheet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class Serializer {
    static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
    }

    static String serialize(Object value) {
        try {
            return value == null ? "" : objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            System.err.println(">>>> Exception when serializating to JSON\n value: " + value + "\n message:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
