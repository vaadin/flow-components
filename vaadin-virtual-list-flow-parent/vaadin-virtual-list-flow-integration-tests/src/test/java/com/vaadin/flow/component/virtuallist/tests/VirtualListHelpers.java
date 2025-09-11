/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.virtuallist.tests;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.flow.internal.JacksonUtils;

public class VirtualListHelpers {

    public static ArrayNode getItems(WebDriver driver, WebElement element) {
        Object result = ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].items;", element);
        ArrayNode array = JacksonUtils.createArrayNode();
        if (!(result instanceof List)) {
            return array;
        }
        List<Map<String, ?>> list = (List<Map<String, ?>>) result;
        for (int i = 0; i < list.size(); i++) {
            Map<String, ?> map = list.get(i);
            if (map != null) {
                ObjectNode obj = JacksonUtils.createObjectNode();
                map.entrySet().forEach(entry -> {
                    obj.put(entry.getKey(), String.valueOf(entry.getValue()));
                });
                array.add(obj);
            } else {
                array.add(JacksonUtils.nullNode());
            }
        }
        return array;
    }

    public static String getPropertyString(ObjectNode json,
            String propertyName) {
        var property = json.properties().stream()
                .filter(prop -> prop.getKey().endsWith(propertyName))
                .findFirst().orElseThrow(() -> new AssertionError(
                        "No property ending with " + propertyName));
        return json.get(property.getKey()).asText();
    }
}
