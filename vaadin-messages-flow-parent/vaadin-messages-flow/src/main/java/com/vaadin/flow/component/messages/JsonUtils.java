/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.messages;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

/**
 * Util methods which are missing from V14 version of Flow's
 * {@link com.vaadin.flow.internal.JsonUtils} class, but which exist in later
 * Flow versions that depend on Jackson.
 *
 * @author Vaadin Ltd.
 */
class JsonUtils {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static JsonObject beanToJson(Object bean) {
        Objects.requireNonNull(bean);
        try {
            return Json.parse(objectMapper.writeValueAsString(bean));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting bean to JSON", e);
        }
    }

    static JsonArray listToJson(List<?> list) {
        Objects.requireNonNull(list);
        try {
            return Json.instance().parse(objectMapper.writeValueAsString(list));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting list to JSON", e);
        }
    }
}
