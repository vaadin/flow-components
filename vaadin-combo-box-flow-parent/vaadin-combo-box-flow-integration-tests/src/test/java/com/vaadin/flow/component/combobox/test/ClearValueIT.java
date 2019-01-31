/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("clear-value")
public class ClearValueIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-combo-box"))
                .size() == 2);
    }

    @Test
    public void valueIsCorrectlyCleared() {
        checkEmptyValue(ClearValuePage.COMBO_BOX_ID,
                ClearValuePage.BUTTON_CLEAR_ID, false);
    }

    @Test
    public void valueIsCorrectlySetToNull() {
        Assert.assertNull(
                "Combobox empty value is not null, add clear tests also",
                new ComboBox<>().getEmptyValue());
        checkEmptyValue(ClearValuePage.COMBO_BOX_ID,
                ClearValuePage.BUTTON_SET_NULL_ID, false);
    }

    @Test
    public void valueIsCorrectlyCleared_allowCustomValue() {
        checkEmptyValue(ClearValuePage.COMBO_BOX_WITH_ALLOW_CUSTOM_VALUE_ID,
                ClearValuePage.BUTTON_CUSTOM_VALUE_CLEAR_ID, true);
    }

    @Test
    public void valueIsCorrectlySetToNull_allowCustomValue() {
        Assert.assertNull(
                "Combobox empty value is not null, add clear tests also",
                new ComboBox<>().getEmptyValue());
        checkEmptyValue(ClearValuePage.COMBO_BOX_WITH_ALLOW_CUSTOM_VALUE_ID,
                ClearValuePage.BUTTON_CUSTOM_VALUE_SET_NULL_ID, true);
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
                comboBox.getAttribute("allowCustomValue"));

        findElement(By.id(buttonId)).click();

        Assert.assertEquals(String.format(
                "Combo box with id '%s' should have its value empty after the test",
                comboBoxId), "", comboBox.getSelectedText());
    }
}
