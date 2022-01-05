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

package com.vaadin.flow.component.select.test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.select.examples.OverrideClientValidationPage;
import com.vaadin.flow.component.select.testbench.SelectElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-select/override-client-validation")
public class OverrideClientValidationIT extends AbstractComponentIT {

    private SelectElement basicSelectElement;
    private TestBenchElement setInvalidButton;
    private TestBenchElement logButton;
    private TestBenchElement detachButton;
    private TestBenchElement reattachButton;
    private TestBenchElement basicSelectResultSpan;
    private SelectElement selectInGridElement;

    @Before
    public void setUp() {
        open();
        basicSelectElement = $(SelectElement.class)
                .id(OverrideClientValidationPage.ID_BASIC_SELECT);
        setInvalidButton = $("button")
                .id(OverrideClientValidationPage.ID_SET_INVALID_BUTTON);
        logButton = $("button").id(OverrideClientValidationPage.ID_LOG_BUTTON);
        detachButton = $("button")
                .id(OverrideClientValidationPage.ID_DETACH_BUTTON);
        reattachButton = $("button")
                .id(OverrideClientValidationPage.ID_REATTACH_BUTTON);
        basicSelectResultSpan = $("span")
                .id(OverrideClientValidationPage.ID_BASIC_SELECT_RESULT_SPAN);
        GridElement gridElement = $(GridElement.class).first();
        selectInGridElement = gridElement.$(SelectElement.class).first();
    }

    @Test
    public void testTriggeringClientValidationShouldNotOverrideClientValidationState() {
        // Set server state to invalid
        setInvalidButton.click();
        assertClientSideSelectValidationState(basicSelectElement, false);

        // Trigger client side validation
        triggerClientSideValidation(basicSelectElement);
        // Client side state should still be invalid
        assertClientSideSelectValidationState(basicSelectElement, false);
    }

    @Test
    public void testModifyingClientSideValidationStateShouldNotAffectServerSideValidationState() {
        // Set server state to invalid
        setInvalidButton.click();
        logButton.click();
        Assert.assertEquals("invalid", basicSelectResultSpan.getText());

        // Overwrite client side validation state to be valid
        overwriteClientSideValidationState(basicSelectElement, true);
        // Server state should still be invalid
        logButton.click();
        Assert.assertEquals("invalid", basicSelectResultSpan.getText());
    }

    @Test
    public void testDetachingAndReattachingShouldStillOverrideClientValidation() {
        // Set server state to invalid
        setInvalidButton.click();
        assertClientSideSelectValidationState(basicSelectElement, false);

        // Detach and reattach
        detachButton.click();
        reattachButton.click();
        // Need to refresh Selenium reference
        basicSelectElement = $(SelectElement.class)
                .id(OverrideClientValidationPage.ID_BASIC_SELECT);

        // Client side state should still be invalid after reattaching
        assertClientSideSelectValidationState(basicSelectElement, false);

        // Trigger client side validation after reattaching
        triggerClientSideValidation(basicSelectElement);
        // Client side state should still be invalid after reattaching and
        // triggering validation
        assertClientSideSelectValidationState(basicSelectElement, false);
    }

    @Test
    public void testTriggeringClientValidationShouldNotOverrideClientValidationStateWhenUsedInComponentRenderer() {
        // Set server state to invalid
        setInvalidButton.click();
        assertClientSideSelectValidationState(selectInGridElement, false);

        triggerClientSideValidation(selectInGridElement);

        assertClientSideSelectValidationState(selectInGridElement, false);
    }

    private void assertClientSideSelectValidationState(
            SelectElement selectElement, boolean valid) {
        Boolean validationState = selectElement.getPropertyBoolean("invalid");

        Assert.assertEquals("Validation state did not match", !valid,
                validationState);
    }

    private void triggerClientSideValidation(SelectElement selectElement) {
        selectElement.getCommandExecutor()
                .executeScript("arguments[0].validate()", selectElement);
        getCommandExecutor().waitForVaadin();
    }

    private void overwriteClientSideValidationState(SelectElement selectElement,
            boolean valid) {
        selectElement.setProperty("invalid", !valid);
        getCommandExecutor().waitForVaadin();
    }
}
