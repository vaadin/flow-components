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
package com.vaadin.flow.component.combobox.test;

import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-combo-box/custom-value")
public class CustomValueIT extends AbstractComboBoxIT {

    ComboBoxElement combo;

    @Before
    public void init() {
        open();
        combo = $(ComboBoxElement.class).first();
    }

    @Test
    public void inputKeys_noEvents() {
        combo.sendKeys("bar");
        assertCustomValueChanges();
        assertValueChanges();
    }

    @Test
    public void inputAndEnter_customValueSetEventFiredWithInputValue() {
        combo.sendKeys("bar", Keys.ENTER);
        assertCustomValueChanges("bar");
    }

    @Test
    public void inputAndEnter_comboBoxValueNotChanged() {
        combo.sendKeys("bar", Keys.ENTER);
        assertValueChanges();
    }

    @Test
    public void typeAndEnterExistingValue_noCustomValueChanges() {
        combo.sendKeys("foo");
        assertRendered("foo");
        combo.sendKeys(Keys.ENTER);
        assertCustomValueChanges();
    }

    @Test
    public void typeAndEnterExistingValue_valueChanged() {
        combo.sendKeys("foo");
        assertRendered("foo");
        combo.sendKeys(Keys.ENTER);
        assertValueChanges("foo");
    }

    @Test
    public void enterCustomValue_clearAndEnter_noNewEvents() {
        combo.sendKeys("bar", Keys.ENTER);
        repeatKey(Keys.BACK_SPACE, 3);
        combo.sendKeys(Keys.ENTER);
        assertCustomValueChanges("bar");
        assertValueChanges();
    }

    @Test
    public void enterExistingValue_clearAndEnter_noCustomValueChange() {
        combo.sendKeys("foo");
        assertRendered("foo");
        combo.sendKeys(Keys.ENTER);
        repeatKey(Keys.BACK_SPACE, 3);
        combo.sendKeys(Keys.ENTER);
        assertCustomValueChanges();
    }

    @Test
    public void enterExistingValue_clearAndEnter_valueChangedToNull() {
        combo.sendKeys("foo");
        assertRendered("foo");
        combo.sendKeys(Keys.ENTER);
        repeatKey(Keys.BACK_SPACE, 3);
        combo.sendKeys(Keys.ENTER);
        assertValueChanges("foo", "null");
    }

    @Test
    public void setCustomValueAsValue_enterCustomValue_valueChanged() {
        clickButton("set-custom-values-as-value");
        combo.sendKeys("bar", Keys.ENTER);
        assertCustomValueChanges("bar");
        assertValueChanges("bar");
    }

    @Test
    // Executing more complex sequence in a common use case
    public void addCustomValueToDataAndSetAsValue_testUseCase() {
        clickButton("add-custom-values-to-data");
        clickButton("set-custom-values-as-value");

        combo.sendKeys("bar", Keys.ENTER);
        assertCustomValueChanges("bar");
        assertValueChanges("bar");

        combo.openPopup();
        waitUntilTextInContent("bar");
        assertLoadedItemsCount(
                "Expected 2 items to be loaded after adding the custom value",
                2, combo);
        assertRendered("foo");
        assertRendered("bar");
        assertItemSelected("bar");

        repeatKey(Keys.BACK_SPACE, 3);
        combo.sendKeys("foo", Keys.ENTER);
        assertCustomValueChanges("bar");
        assertValueChanges("bar", "foo");
        combo.openPopup();
        assertLoadedItemsCount("Expected 2 items to be loaded", 2, combo);
        assertItemSelected("foo");

        combo.sendKeys("baz", Keys.ENTER);
        assertCustomValueChanges("bar", "foobaz");
        assertValueChanges("bar", "foo", "foobaz");
        combo.openPopup();
        assertLoadedItemsCount(
                "Expected 3 items to be loaded after adding the custom value",
                3, combo);
        assertItemSelected("foobaz");

        repeatKey(Keys.BACK_SPACE, 6);
        combo.sendKeys(Keys.ENTER);

        assertCustomValueChanges("bar", "foobaz");
        assertValueChanges("bar", "foo", "foobaz", "null");
    }

    private void assertCustomValueChanges(String... expected) {
        assertMessages(true, expected);
    }

    private void assertValueChanges(String... expected) {
        assertMessages(false, expected);
    }

    private String[] messages;

    private void assertMessages(boolean customValues, String... expected) {
        String containerId = customValues ? "custom-value-messages"
                : "value-messages";
        String valueName = customValues ? "custom value" : "value";

        try {
            waitUntil(driver -> {
                messages = $(TestBenchElement.class).id(containerId).$("p")
                        .all().stream().map(TestBenchElement::getText)
                        .toArray(String[]::new);
                return messages.length == expected.length;
            }, 5);
        } catch (Exception e) {
            Assert.fail(String.format(
                    "\nExpected %s changes: [%s]\nbut was: [%s]\n", valueName,
                    String.join(", ", expected), String.join(", ", messages)));
        }
        Assert.assertArrayEquals(
                String.format("Unexpected %s changes for ComboBox", valueName),
                expected, messages);
    }

    private void repeatKey(Keys key, int amount) {
        IntStream.range(0, amount).forEach(i -> combo.sendKeys(key));
    }

}
