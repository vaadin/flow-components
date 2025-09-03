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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.flow.component.upload.UploadI18N;
import com.vaadin.flow.component.upload.testbench.UploadElement;
import com.vaadin.flow.internal.JacksonSerializer;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-upload/i18n")
public class UploadI18nIT extends AbstractUploadIT {
    @Test
    public void testFullI18nShouldAffectLabels() {
        open();

        UploadElement upload = $(UploadElement.class).id("upload-full-i18n");
        WebElement addButton = upload.$("*").withAttribute("slot", "add-button")
                .first();
        WebElement dropLabel = upload.$("*").withAttribute("slot", "drop-label")
                .first();

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
        ObjectNode i18nJson = getUploadI18nPropertyAsJson(upload);
        Map<String, String> translationMap = jsonToMap(i18nJson);

        UploadI18N expected = UploadTestsI18N.RUSSIAN_FULL;
        ObjectNode expectedJson = JacksonUtils.getMapper()
                .valueToTree(expected);
        deeplyRemoveNullValuesFromJsonObject(expectedJson);
        Map<String, String> expectedMap = jsonToMap(expectedJson);

        assertTranslationMapsAreEqual(expectedMap, translationMap);
    }

    @Test
    public void testPartialI18nShouldAffectLabels() {
        open();

        UploadElement upload = $(UploadElement.class).id("upload-partial-i18n");
        WebElement addButton = upload.$("*").withAttribute("slot", "add-button")
                .first();
        WebElement dropLabel = upload.$("*").withAttribute("slot", "drop-label")
                .first();

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
        ObjectNode i18nJson = getUploadI18nPropertyAsJson(upload);
        Map<String, String> translationMap = jsonToMap(i18nJson);

        UploadI18N fullTranslation = UploadTestsI18N.RUSSIAN_FULL;
        ObjectNode fullTranslationJson = (ObjectNode) JacksonSerializer
                .toJson(fullTranslation);
        deeplyRemoveNullValuesFromJsonObject(fullTranslationJson);
        Map<String, String> fullTranslationMap = jsonToMap(fullTranslationJson);
        UploadTestsI18N.OPTIONAL_KEYS.forEach(fullTranslationMap::remove);

        assertTranslationMapsHaveSameKeys(fullTranslationMap, translationMap);
        assertTranslationMapHasNoMissingTranslations(translationMap);
    }

    @Test
    public void testDetachReattachI18nIsPreserved() {
        open();

        WebElement btnSetI18n = findElement(By.id("btn-set-i18n"));
        WebElement btnToggleAttached = findElement(
                By.id("btn-toggle-attached"));

        btnSetI18n.click();

        btnToggleAttached.click();
        btnToggleAttached.click();

        UploadElement upload = $(UploadElement.class)
                .id("upload-detach-reattach-i18n");

        WebElement dropLabel = upload.$("*").withAttribute("slot", "drop-label")
                .first();

        Assert.assertEquals(
                UploadTestsI18N.RUSSIAN_FULL.getDropFiles().getOne(),
                dropLabel.getText());
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
            Assert.assertTrue("Missing translation key: " + expectedKey,
                    actual.containsKey(expectedKey));
        });
    }

    private void assertTranslationMapHasNoMissingTranslations(
            Map<String, String> map) {
        map.keySet().forEach(key -> {
            String value = map.get(key);
            Assert.assertNotNull("Missing translation value: " + key, value);
        });
    }

    /**
     * Converts a deeply nested ObjectNode into a Map of key / value pairs,
     * where the key is the path through the object to the property, and the
     * value is the string value of the property, or null if the property was
     * null
     */
    private Map<String, String> jsonToMap(ObjectNode jsonNode) {
        return jsonToMap(new HashMap<>(), "", jsonNode);
    }

    private Map<String, String> jsonToMap(Map<String, String> output,
            String path, ObjectNode node) {
        node.forEachEntry((key, value) -> {
            String subPath = path.isEmpty() ? key : path + "." + key;

            if (value.isObject()) {
                jsonToMap(output, subPath, (ObjectNode) value);
            } else if (value.isNull()) {
                output.put(subPath, null);
            } else {
                String stringValue = value.asText();
                output.put(subPath, stringValue);
            }
        });
        return output;
    }

    private ObjectNode getUploadI18nPropertyAsJson(UploadElement upload) {
        String i18nJsonString = (String) upload.getCommandExecutor()
                .executeScript("return JSON.stringify(arguments[0].i18n)",
                        upload);
        return JacksonUtils.readTree(i18nJsonString);
    }

    private void deeplyRemoveNullValuesFromJsonObject(ObjectNode jsonObject) {
        List<String> keysToRemove = new ArrayList<>();
        jsonObject.forEachEntry((key, value) -> {
            if (value.isObject()) {
                deeplyRemoveNullValuesFromJsonObject((ObjectNode) value);
            }
            if (value.isNull()) {
                keysToRemove.add(key);
            }
        });
        keysToRemove.forEach(jsonObject::remove);
    }
}
