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
package com.vaadin.flow.component.textfield.tests.validation;

import static com.vaadin.flow.component.textfield.tests.validation.AbstractValueChangeModeValidationPage.RESET_VALIDATION_LOG_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.AbstractValueChangeModeValidationPage.VALIDATION_LOG;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

import com.vaadin.testbench.HasValidation;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.validation.AbstractValidationIT;

public abstract class AbstractValueChangeModeValidationIT<T extends HasValidation>
        extends AbstractValidationIT<T> {
    private long validationTimeoutStart;

    protected List<String> getValidationResults() {
        return $("div").id(VALIDATION_LOG).$("div").all().stream()
                .map(TestBenchElement::getText).toList();
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
