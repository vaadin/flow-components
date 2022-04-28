package com.vaadin.flow.component.spreadsheet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class Serializer {
    private final static ObjectMapper objectMapper;
    private final static Logger LOGGER = LoggerFactory
            .getLogger(Serializer.class);

    static {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
    }

    static String serialize(Object value) {
        try {
            return value == null ? "" : objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error when serializating to JSON\n value: " + value,
                    e);
            return null;
        }
    }
}
