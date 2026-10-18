/**
 * Copyright 2000-2026 Vaadin Ltd.
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
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.BaseJsonNode;
import tools.jackson.databind.node.NullNode;

class Serializer {
    private final static ObjectMapper objectMapper;
    private final static Logger LOGGER = LoggerFactory
            .getLogger(Serializer.class);

    static {
        // Defaults are left off the wire, which keeps a cell update to
        // the fields that actually carry something.
        //
        // A payload that is a whole snapshot rather than an update
        // cannot afford that - a 0 it leaves out reads as undefined on
        // the client, not as 0 - and says so with its own
        // {@code @JsonInclude(ALWAYS)}.
        objectMapper = JsonMapper.builder()
                .changeDefaultPropertyInclusion(handler -> handler
                        .withValueInclusion(JsonInclude.Include.NON_DEFAULT))
                .build();
    }

    /**
     * The value as JSON, which goes to the client as it is. Sent as text inside
     * the message, every quote of it was escaped, a sixth of what a cell took
     * on the wire, and the client parsed it a second time. {@code null} is
     * JSON's null.
     */
    static BaseJsonNode toJson(Object value) {
        if (value == null) {
            return NullNode.getInstance();
        }
        try {
            return objectMapper.valueToTree(value);
        } catch (JacksonException e) {
            LOGGER.error("Error when converting to JSON: {}", value, e);
            return NullNode.getInstance();
        }
    }

    /** An empty JSON array, for a list built one item at a time. */
    static ArrayNode array() {
        return objectMapper.createArrayNode();
    }
}
