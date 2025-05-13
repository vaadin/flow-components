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
package com.vaadin.flow.component.combobox.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-combo-box/clear-value")
public class ClearValueIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-combo-box"))
                .size() == 3);
    }

    @Test
    public void valueIsCorrectlyCleared() {
        checkEmptyValue(ClearValuePage.COMBO_BOX,
                ClearValuePage.COMBO_BOX_CLEAR_BUTTON, false);
    }

    @Test
    public void valueIsCorrectlyClearedWithClearButtonBeforeOpened() {
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id(ClearValuePage.COMBO_BOX_WITH_CLEAR_BUTTON);
        Assert.assertEquals(
                "Unexpected selected item label for combo box with clear button",
                ClearValuePage.INITIAL_VALUE, comboBox.getSelectedText());

        comboBox.$("[part~='clear-button']").get(0).click();

        Assert.assertEquals(
                "Combo box with clear button should have its value empty after the test",
                "null",
                $(TestBenchElement.class).id(
                        ClearValuePage.COMBO_BOX_WITH_CLEAR_BUTTON_VALUE_MESSAGES)
                        .$("p").first().getText());
    }

    @Test
    public void openPopup_clearButton_selectedItemIsReset() {
        String comboBoxId = ClearValuePage.COMBO_BOX_WITH_CLEAR_BUTTON;
        ComboBoxElement comboBox = $(ComboBoxElement.class).id(comboBoxId);

        comboBox.openPopup();
        comboBox.closePopup();

        comboBox.clickClearButton();

        comboBox.openPopup();

        TestBenchElement overlay = $("vaadin-combo-box-overlay").first();
        ElementQuery<TestBenchElement> items = overlay
                .$("vaadin-combo-box-item");

        items.all()
                .forEach(item -> Assert.assertFalse(
                        "Item is not selected after clear button click",
                        item.hasAttribute("selected")));
    }

    @Test
    public void valueIsCorrectlySetToNull() {
        Assert.assertNull(
                "Combobox empty value is not null, add clear tests also",
                new ComboBox<>().getEmptyValue());
        checkEmptyValue(ClearValuePage.COMBO_BOX,
                ClearValuePage.COMBO_BOX_SET_NULL_VALUE_BUTTON, false);
    }

    @Test
    public void allowCustomValue_setInitialValue_valueIsCorrectlyCleared() {
        checkEmptyValue(ClearValuePage.COMBO_BOX_WITH_ALLOW_CUSTOM_VALUE,
                ClearValuePage.COMBO_BOX_WITH_ALLOW_CUSTOM_VALUE_CLEAR_BUTTON,
                true);
    }

    @Test
    public void allowCustomValue_setInitialValue_valueIsCorrectlySetToNull() {
        Assert.assertNull(
                "Combobox empty value is not null, add clear tests also",
                new ComboBox<>().getEmptyValue());
        checkEmptyValue(ClearValuePage.COMBO_BOX_WITH_ALLOW_CUSTOM_VALUE,
                ClearValuePage.COMBO_BOX_WITH_ALLOW_CUSTOM_VALUE_SET_NULL_VALUE_BUTTON,
                true);
    }

    @Test
    public void allowCustomValue_enterCustomValue_clearValue_inputElementValueIsCleared() {
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id(ClearValuePage.COMBO_BOX_WITH_ALLOW_CUSTOM_VALUE);
        TestBenchElement clearButton = $("button").id(
                ClearValuePage.COMBO_BOX_WITH_ALLOW_CUSTOM_VALUE_CLEAR_BUTTON);

        // Clear initial value to set the state of the input element value
        // property to an empty value
        clearButton.click();
        Assert.assertEquals("", comboBox.getInputElementValue());

        // Enter custom value
        comboBox.sendKeys("foo", Keys.ENTER);
        Assert.assertEquals("foo", comboBox.getInputElementValue());

        // Clear value
        clearButton.click();
        Assert.assertEquals("", comboBox.getInputElementValue());
    }

    @Test
    public void allowCustomValue_enterCustomValue_setNullValue_inputElementValueIsCleared() {
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id(ClearValuePage.COMBO_BOX_WITH_ALLOW_CUSTOM_VALUE);
        TestBenchElement setNullValueButton = $("button").id(
                ClearValuePage.COMBO_BOX_WITH_ALLOW_CUSTOM_VALUE_SET_NULL_VALUE_BUTTON);

        // Set null value to set the state of the input element value property
        // to an empty value
        setNullValueButton.click();
        Assert.assertEquals("", comboBox.getInputElementValue());

        // Enter custom value
        comboBox.sendKeys("foo", Keys.ENTER);
        Assert.assertEquals("foo", comboBox.getInputElementValue());

        // Set null value
        setNullValueButton.click();
        Assert.assertEquals("", comboBox.getInputElementValue());
    }

    @Test
    public void allowCustomValue_enterNotAcceptableCustomValue_inputElementValueIsCleared() {
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id(ClearValuePage.COMBO_BOX_WITH_ALLOW_CUSTOM_VALUE);

        TestBenchElement clearButton = $("button").id(
                ClearValuePage.COMBO_BOX_WITH_ALLOW_CUSTOM_VALUE_CLEAR_BUTTON);

        // Clear initial value to set the state of the input element value
        // property to an empty value
        clearButton.click();
        Assert.assertEquals("", comboBox.getInputElementValue());

        // Enter a custom value that is cleared in the `customValueSet`
        // listener.
        comboBox.sendKeys("NotAcceptableCustomValue", Keys.ENTER);
        Assert.assertEquals("", comboBox.getInputElementValue());
    }

    private void checkEmptyValue(String comboBoxId, String buttonId,
            boolean allowCustomValue) {
        ComboBoxElement comboBox = $(ComboBoxElement.class).id(comboBoxId);
        Assert.assertEquals(String.format(
                "Unexpected selected item label for combo box with id '%s'",
                comboBoxId), ClearValuePage.INITIAL_VALUE,
                comboBox.getSelectedText());
        Assert.assertEquals(String.format(
                "Unexpected 'allowCustomValue' property name for combo box with id '%s'",
                comboBoxId), Boolean.toString(allowCustomValue),
                comboBox.getPropertyString("allowCustomValue"));

        findElement(By.id(buttonId)).click();

        Assert.assertEquals(String.format(
                "Combo box with id '%s' should have its value empty after the test",
                comboBoxId), "", comboBox.getSelectedText());
    }
}
