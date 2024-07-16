/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.testbench.HasStringValueProperty;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.commands.TestBenchCommandExecutor;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

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
