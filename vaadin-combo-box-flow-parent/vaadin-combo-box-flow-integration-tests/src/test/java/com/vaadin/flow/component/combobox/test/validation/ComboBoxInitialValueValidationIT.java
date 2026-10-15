/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
