/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.combobox.test.validation;

import static com.vaadin.flow.component.combobox.test.validation.ComboBoxInitialValueValidationPage.REQUIRED_ERROR_MESSAGE;
import static com.vaadin.flow.component.combobox.test.validation.ComboBoxInitialValueValidationPage.SET_EMPTY_STRING;
import static com.vaadin.flow.component.combobox.test.validation.ComboBoxInitialValueValidationPage.SET_NULL;
import static com.vaadin.tests.validation.AbstractValidationPage.ATTACH_FIELD_BUTTON;
import static com.vaadin.tests.validation.AbstractValidationPage.DETACH_FIELD_BUTTON;

import org.junit.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

@TestPath("vaadin-combo-box/validation/initial-value")
public class ComboBoxInitialValueValidationIT
        extends AbstractValidationIT<ComboBoxElement> {

    @Test
    public void fieldWithEmptyString_fieldIsValid() {
        detachButton();

        clickButton(SET_EMPTY_STRING);

        attachButton();
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void requiredFieldWithNull_fieldIsInvalid() {
        detachButton();

        // Need to "force" value change on the field
        clickButton(SET_EMPTY_STRING);
        clickButton(SET_NULL);

        attachButton();
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    private void attachButton() {
        clickButton(ATTACH_FIELD_BUTTON);
        // Retrieve new element instance
        testField = getTestField();
    }

    private void detachButton() {
        clickButton(DETACH_FIELD_BUTTON);
        // Verify element has been removed
        waitUntil(ExpectedConditions.stalenessOf(testField));
    }

    private void clickButton(String buttonId) {
        $("button").id(buttonId).click();
    }

    @Override
    protected ComboBoxElement getTestField() {
        return $(ComboBoxElement.class).first();
    }
}
