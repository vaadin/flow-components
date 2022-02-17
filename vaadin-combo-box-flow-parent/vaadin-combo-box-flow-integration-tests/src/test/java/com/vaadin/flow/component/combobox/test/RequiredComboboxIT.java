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

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-combo-box/required-combobox")
public class RequiredComboboxIT extends AbstractComponentIT {

    @Test
    public void serverSideValidation_persistsOnBlur() {
        open();

        ComboBoxElement comboBox = $(ComboBoxElement.class).first();

        // Select an invalid item
        comboBox.openPopup();
        executeScript(
                "arguments[0].selectedItem = arguments[0].filteredItems[0]",
                comboBox);

        // The validation shows errors
        assertValidationError(comboBox);

        TestBenchElement msg = $(TestBenchElement.class).id("message");
        Assert.assertEquals("Value changed from 'null' to 'foo'",
                msg.getText());

        // blur
        msg.click();

        // validation error is still shown
        assertValidationError(comboBox);

        // change the item to a valid one
        comboBox.openPopup();
        executeScript(
                "arguments[0].selectedItem = arguments[0].filteredItems[1]",
                comboBox);

        // no invalid attribute
        Assert.assertEquals(Boolean.FALSE.toString(),
                comboBox.getAttribute("invalid"));
        // the error message is not visible
        TestBenchElement error = comboBox.$("[part='error-message']").first();
        waitUntil(driver -> error.getSize().getHeight() == 0);
    }

    @Test
    public void setItemsAfterSettingRequired_noClientSideError() {
        open();
        checkLogsForErrors();
    }

    private void assertValidationError(ComboBoxElement comboBox) {
        Assert.assertEquals(Boolean.TRUE.toString(),
                comboBox.getAttribute("invalid"));

        TestBenchElement error = comboBox.$("[part='error-message']").first();
        Assert.assertTrue(error.getSize().getHeight() > 0);
        Assert.assertEquals("'foo' is invalid value", error.getText());
    }
}
