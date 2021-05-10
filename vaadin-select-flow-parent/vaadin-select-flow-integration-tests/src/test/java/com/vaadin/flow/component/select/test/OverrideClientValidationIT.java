/*
 * Copyright 2000-2021 Vaadin Ltd.
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

import com.vaadin.flow.component.select.testbench.SelectElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-select/override-client-validation")
public class OverrideClientValidationIT extends AbstractComponentIT {

    @Test
    public void testTriggeringClientValidationShouldNotOverrideBinderValidationResults() {
        open();
        WebElement validateButton = findElement(By.id("validate-button"));

        // Trigger binder validation and assert invalid state
        validateButton.click();
        getCommandExecutor().waitForVaadin();
        assertClientSideSelectValidationState(false);
        // Trigger client side validation
        triggerClientSideValidation();
        // Client side state should still be invalid
        assertClientSideSelectValidationState(false);
    }

    @Test
    public void testModifyingClientSideValidationStateShouldNotAffectServerSideValidationState() {
        open();
        WebElement validateButton = findElement(By.id("validate-button"));
        WebElement logValidationStateButton = findElement(
                By.id("log-validation-state-button"));
        WebElement validationStateSpan = findElement(
                By.id("validation-state-span"));

        // Trigger binder validation and assert invalid state
        validateButton.click();
        getCommandExecutor().waitForVaadin();
        logValidationStateButton.click();
        getCommandExecutor().waitForVaadin();
        Assert.assertEquals("invalid", validationStateSpan.getText());
        // Overwrite client side validation state to be valid
        overwriteClientSideValidationState(true);
        getCommandExecutor().waitForVaadin();
        // Server state should still be invalid
        logValidationStateButton.click();
        Assert.assertEquals("invalid", validationStateSpan.getText());
    }

    private void assertClientSideSelectValidationState(boolean valid) {
        SelectElement selectElement = getSelectElement();
        Boolean validationState = selectElement.getPropertyBoolean("invalid");

        Assert.assertEquals("Validation state did not match", !valid,
                validationState);
    }

    private void triggerClientSideValidation() {
        SelectElement selectElement = getSelectElement();
        selectElement.getCommandExecutor()
                .executeScript("arguments[0].validate()", selectElement);
        getCommandExecutor().waitForVaadin();
    }

    private void overwriteClientSideValidationState(boolean valid) {
        SelectElement selectElement = getSelectElement();
        selectElement.setProperty("invalid", !valid);
    }

    private SelectElement getSelectElement() {
        SelectElement selectElement = $(SelectElement.class).first();
        if (selectElement == null) {
            throw new NoSuchElementException("Can not find select");
        }
        return selectElement;
    }
}
