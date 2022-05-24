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
 *
 */

package com.vaadin.flow.component.upload.tests;

import com.vaadin.flow.component.upload.UploadI18N;
import com.vaadin.flow.component.upload.testbench.UploadElement;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.testutil.TestPath;
import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.JsonType;
import elemental.json.JsonValue;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.Map;

@TestPath("vaadin-upload/i18n")
public class UploadI18nIT extends AbstractUploadIT {
    @Test
    public void testFullI18nShouldAffectLabels() {
        open();

        UploadElement upload = $(UploadElement.class).id("upload-full-i18n");
        WebElement addButton = upload.$("*").id("addButton");
        WebElement dropLabel = upload.$("*").id("dropLabel");

        Assert.assertEquals(UploadTestsI18N.RUSSIAN_FULL.getAddFiles().getOne(),
                addButton.getText());
        Assert.assertEquals(
                UploadTestsI18N.RUSSIAN_FULL.getDropFiles().getOne(),
                dropLabel.getText());
    }

    /**
     * Verifies that every single translation provided by the UploadI18N
     * instance is set in the web component.
     *
     * Testing internals here, in favour of setting up the web component with
     * files/event handlers for every possible state.
     */
    @Test
    public void testFullI18nShouldOverrideCompleteConfigurationInWebComponentProperty() {
        open();

        UploadElement upload = $(UploadElement.class).id("upload-full-i18n");
        JsonObject i18nJson = getUploadI18nPropertyAsJson(upload);
        Map<String, String> translationMap = jsonToMap(i18nJson);

        UploadI18N expected = UploadTestsI18N.RUSSIAN_FULL;
        JsonObject expectedJson = (JsonObject) JsonSerializer.toJson(expected);
        Map<String, String> expectedMap = jsonToMap(expectedJson);

        assertTranslationMapsAreEqual(expectedMap, translationMap);
    }

    @Test
    public void testPartialI18nShouldAffectLabels() {
        open();

        UploadElement upload = $(UploadElement.class).id("upload-partial-i18n");
        WebElement addButton = upload.$("*").id("addButton");
        WebElement dropLabel = upload.$("*").id("dropLabel");

        // This label should still be the default one
        Assert.assertEquals("Upload File...", addButton.getText());
        // This one should be overwritten by the UploadI18N config
        Assert.assertEquals(
                UploadTestsI18N.RUSSIAN_PARTIAL.getDropFiles().getOne(),
                dropLabel.getText());
    }

    /**
     * Verifies that setting a partial UploadI18N configuration still results in
     * a complete configuration in the web component, and that null values in
     * the UploadI18N configuration are ignored.
     *
     * Testing internals here, in favour of setting up the web component with
     * files/event handlers for every possible state.
     */
    @Test
    public void testPartialI18nShouldSetFullConfigurationWithoutNullValuesInWebComponentProperty() {
        open();

        UploadElement upload = $(UploadElement.class).id("upload-partial-i18n");
        JsonObject i18nJson = getUploadI18nPropertyAsJson(upload);
        Map<String, String> translationMap = jsonToMap(i18nJson);

        UploadI18N fullTranslation = UploadTestsI18N.RUSSIAN_FULL;
        JsonObject fullTranslationJson = (JsonObject) JsonSerializer
                .toJson(fullTranslation);
        Map<String, String> fullTranslationMap = jsonToMap(fullTranslationJson);

        assertTranslationMapsHaveSameKeys(fullTranslationMap, translationMap);
        assertTranslationMapHasNoMissingTranslations(translationMap);
    }

    private void assertTranslationMapsAreEqual(Map<String, String> expected,
            Map<String, String> actual) {
        expected.keySet().forEach(expectedKey -> {
            Assert.assertTrue("Missing translation key: " + expectedKey,
                    actual.containsKey(expectedKey));
            String expectedValue = expected.get(expectedKey);
            String actualValue = actual.get(expectedKey);
            Assert.assertEquals(
                    String.format("Mismatching translation: %s!=%s",
                            expectedValue, actualValue),
                    expectedValue, actualValue);
        });
    }

    private void assertTranslationMapsHaveSameKeys(Map<String, String> expected,
            Map<String, String> actual) {
        expected.keySet().forEach(expectedKey -> {
            // Cancel was removed in
            // https://github.com/vaadin/web-components/pull/2723
            if (!"cancel".equals(expectedKey)) {
                Assert.assertTrue("Missing translation key: " + expectedKey,
                        actual.containsKey(expectedKey));
            }
        });
    }

    private void assertTranslationMapHasNoMissingTranslations(
            Map<String, String> map) {
        map.keySet().forEach(key -> {
            // Cancel was removed in
            // https://github.com/vaadin/web-components/pull/2723
            if (!"cancel".equals(key)) {
                String value = map.get(key);
                Assert.assertNotNull("Missing translation value: " + key,
                        value);
            }
        });
    }

    /**
     * Converts a deeply nested JsonObject into a Map of key / value pairs,
     * where the key is the path through the object to the property, and the
     * value is the string value of the property, or null if the property was
     * null
     */
    private Map<String, String> jsonToMap(JsonObject jsonObject) {
        return jsonToMap(new HashMap<>(), "", jsonObject);
    }

    private Map<String, String> jsonToMap(Map<String, String> output,
            String path, JsonObject node) {
        for (String key : node.keys()) {
            JsonValue jsonValue = node.get(key);
            String subPath = path.isEmpty() ? key : path + "." + key;

            if (jsonValue.getType() == JsonType.OBJECT) {
                jsonToMap(output, subPath, (JsonObject) jsonValue);
            } else if (jsonValue.getType() == JsonType.NULL) {
                output.put(subPath, null);
            } else {
                String stringValue = jsonValue.asString();
                output.put(subPath, stringValue);
            }
        }
        return output;
    }

    private JsonObject getUploadI18nPropertyAsJson(UploadElement upload) {
        String i18nJsonString = (String) upload.getCommandExecutor()
                .executeScript("return JSON.stringify(arguments[0].i18n)",
                        upload);
        return Json.parse(i18nJsonString);
    }
}
