/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests.validation;

import static com.vaadin.flow.component.textfield.tests.validation.AbstractValueChangeModeValidationPage.RESET_VALIDATION_LOG_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.AbstractValueChangeModeValidationPage.VALIDATION_LOG;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.validation.AbstractValidationIT;

public abstract class AbstractValueChangeModeValidationIT<T extends TestBenchElement>
        extends AbstractValidationIT<T> {
    private long validationTimeoutStart;

    protected List<String> getValidationResults() {
        return $("div").id(VALIDATION_LOG).$("div").all().stream()
                .map(record -> record.getText()).toList();
    }

    protected void startValidationTimeout() {
        validationTimeoutStart = System.currentTimeMillis();
    }

    protected void assertValidationTimeout(int expected) {
        // Wait for validation to be run
        waitUntil(e -> !getValidationResults().isEmpty());

        long actual = System.currentTimeMillis() - validationTimeoutStart;

        Assert.assertTrue("The validation was triggered in " + actual
                + "ms (expected " + expected + "ms)", actual >= expected);
    }

    protected void assertValidationResults(String... expectedResults) {
        Assert.assertEquals(Arrays.asList(expectedResults),
                getValidationResults());
        resetValidationLog();
    }

    protected void resetValidationLog() {
        $("button").id(RESET_VALIDATION_LOG_BUTTON).click();
    }
}
