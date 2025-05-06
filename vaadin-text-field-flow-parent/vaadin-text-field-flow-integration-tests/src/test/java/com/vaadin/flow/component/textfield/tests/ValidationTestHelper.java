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
package com.vaadin.flow.component.textfield.tests;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.HasStringValueProperty;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.commands.TestBenchCommandExecutor;

class ValidationTestHelper {

    private ValidationTestHelper() {
    }

    public static <E extends TestBenchElement & HasStringValueProperty> void testValidation(
            TestBenchCommandExecutor commandExecutor,
            SearchContext searchContext, E field) {
        final boolean valid = true;
        assertValidState(searchContext, valid);

        // max is 10
        field.setValue("12345678901");
        assertValidState(searchContext, !valid);

        // Forcing max to 11 on the client does not make the field valid
        field.setProperty("max", "11");
        commandExecutor.waitForVaadin();
        assertValidState(searchContext, !valid);

        // Forcing the field to be valid does not work
        field.setProperty("invalid", false);
        commandExecutor.waitForVaadin();
        assertValidState(searchContext, !valid);

        // Setting a valid value makes the field return to valid mode
        field.setValue("1234567890");
        commandExecutor.waitForVaadin();
        assertValidState(searchContext, valid);
    }

    private static void assertValidState(SearchContext context, boolean valid) {
        final WebElement checkIsInvalid = context
                .findElement(By.id("check-is-invalid"));
        checkIsInvalid.click();

        final String expectedValue = !valid ? "invalid" : "valid";
        Assert.assertEquals(expectedValue,
                context.findElement(By.id("is-invalid")).getText());
    }
}
