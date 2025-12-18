/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class Serializer {
    private final static ObjectMapper objectMapper;
    private final static Logger LOGGER = LoggerFactory
            .getLogger(Serializer.class);

    static {
        objectMapper = JsonMapper.builder()
                .changeDefaultPropertyInclusion(handler -> handler
                        .withValueInclusion(JsonInclude.Include.NON_DEFAULT))
                .build();
    }

    static String serialize(Object value) {
        try {
            return value == null ? "" : objectMapper.writeValueAsString(value);
        } catch (JacksonException e) {
            LOGGER.error("Error when serializing to JSON value: {}", value, e);
            return null;
        }
    }
}
