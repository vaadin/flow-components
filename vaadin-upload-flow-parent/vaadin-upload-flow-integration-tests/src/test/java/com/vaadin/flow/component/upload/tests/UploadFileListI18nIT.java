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
package com.vaadin.flow.component.upload.tests;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.upload.UploadFileListI18N;
import com.vaadin.flow.component.upload.testbench.UploadFileListElement;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.testutil.TestPath;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

@TestPath("vaadin-upload/file-list-i18n")
public class UploadFileListI18nIT extends AbstractUploadIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void testFullI18n_allTranslationsAreApplied() {
        UploadFileListElement fileList = $(UploadFileListElement.class)
                .id("file-list-full");
        ObjectNode i18nJson = getFileListI18nPropertyAsJson(fileList);
        Map<String, String> translationMap = jsonToMap(i18nJson);

        UploadFileListI18N expected = UploadFileListI18nPage.FULL_I18N;
        ObjectNode expectedJson = JacksonUtils.beanToJson(expected);
        deeplyRemoveNullValuesFromJsonObject(expectedJson);
        Map<String, String> expectedMap = jsonToMap(expectedJson);

        assertTranslationMapsAreEqual(expectedMap, translationMap);
    }

    @Test
    public void testPartialI18n_onlyProvidedTranslationsAreOverridden() {
        UploadFileListElement fileList = $(UploadFileListElement.class)
                .id("file-list-partial");
        ObjectNode i18nJson = getFileListI18nPropertyAsJson(fileList);
        Map<String, String> translationMap = jsonToMap(i18nJson);

        // Verify the partial translation is applied
        Assert.assertEquals("\"Poista\"", translationMap.get("file.remove"));

        // Verify other file translations still have their default values
        // (the web component has built-in defaults that are preserved)
        Assert.assertEquals("\"Retry\"", translationMap.get("file.retry"));
        Assert.assertEquals("\"Start\"", translationMap.get("file.start"));
    }

    @Test
    public void testDetachReattach_i18nIsPreserved() {
        WebElement btnSetI18n = findElement(By.id("btn-set-i18n"));
        WebElement btnToggleAttached = findElement(
                By.id("btn-toggle-attached"));

        // Set i18n
        btnSetI18n.click();

        // Detach and reattach
        btnToggleAttached.click();
        btnToggleAttached.click();

        // Verify i18n is still applied
        UploadFileListElement fileList = $(UploadFileListElement.class)
                .id("file-list-detach");
        ObjectNode i18nJson = getFileListI18nPropertyAsJson(fileList);
        Map<String, String> translationMap = jsonToMap(i18nJson);

        UploadFileListI18N expected = UploadFileListI18nPage.FULL_I18N;
        ObjectNode expectedJson = JacksonUtils.beanToJson(expected);
        deeplyRemoveNullValuesFromJsonObject(expectedJson);
        Map<String, String> expectedMap = jsonToMap(expectedJson);

        assertTranslationMapsAreEqual(expectedMap, translationMap);
    }

    private void assertTranslationMapsAreEqual(Map<String, String> expected,
            Map<String, String> actual) {
        expected.keySet().forEach(expectedKey -> {
            Assert.assertTrue("Missing translation key: " + expectedKey,
                    actual.containsKey(expectedKey));
            String expectedValue = expected.get(expectedKey);
            String actualValue = actual.get(expectedKey);
            Assert.assertEquals(
                    String.format(
                            "Mismatching translation for key '%s': %s != %s",
                            expectedKey, expectedValue, actualValue),
                    expectedValue, actualValue);
        });
    }

    private Map<String, String> jsonToMap(ObjectNode jsonObject) {
        return jsonToMap(new HashMap<>(), "", jsonObject);
    }

    private Map<String, String> jsonToMap(Map<String, String> output,
            String path, ObjectNode node) {
        for (String key : node.propertyNames()) {
            JsonNode jsonValue = node.get(key);
            String subPath = path.isEmpty() ? key : path + "." + key;

            if (jsonValue.isObject()) {
                jsonToMap(output, subPath, (ObjectNode) jsonValue);
            } else if (jsonValue.isNull()) {
                output.put(subPath, null);
            } else {
                String stringValue = jsonValue.toString();
                output.put(subPath, stringValue);
            }
        }
        return output;
    }

    private ObjectNode getFileListI18nPropertyAsJson(
            UploadFileListElement fileList) {
        String i18nJsonString = (String) fileList.getCommandExecutor()
                .executeScript("return JSON.stringify(arguments[0].i18n)",
                        fileList);
        return JacksonUtils.readTree(i18nJsonString);
    }

    private void deeplyRemoveNullValuesFromJsonObject(ObjectNode jsonObject) {
        for (String key : jsonObject.propertyNames()) {
            if (jsonObject.get(key).isObject()) {
                deeplyRemoveNullValuesFromJsonObject(
                        (ObjectNode) jsonObject.get(key));
            } else if (jsonObject.get(key).isNull()) {
                jsonObject.remove(key);
            }
        }
    }
}
