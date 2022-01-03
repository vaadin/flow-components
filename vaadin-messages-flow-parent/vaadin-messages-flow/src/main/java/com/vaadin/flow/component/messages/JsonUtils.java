/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
