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
package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.tests.AbstractValidationTest;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;

/**
 * Integration tests for {@link DateTimePicker} validation.
 */
@TestPath("vaadin-date-time-picker/date-time-picker-validation")
public class DateTimePickerValidationPageIT extends AbstractValidationTest {

    @Test
    public void assertInvalidAfterClientChangeMax() {
        // max is 2020-06-07T03:00
        final LocalDateTime invalidDateTime = LocalDateTime.of(2020, 6, 8, 3,
                0);
        final LocalDateTime validDateTime = LocalDateTime.of(2020, 6, 7, 2, 0);
        assertInvalidAfterClientChange("max", invalidDateTime, validDateTime);
    }

    @Test
    public void assertInvalidAfterClientChangeMin() {
        // min is 2020-06-07T01:00
        final LocalDateTime invalidDateTime = LocalDateTime.of(2020, 6, 7, 0,
                30);
        final LocalDateTime validDateTime = LocalDateTime.of(2020, 6, 7, 1, 0);
        assertInvalidAfterClientChange("min", invalidDateTime, validDateTime);
    }

    private void assertInvalidAfterClientChange(String clientPropertyUnderTest,
            LocalDateTime invalidValue, LocalDateTime validValue) {

        final boolean valid = true;
        final DateTimePickerElement element = $(DateTimePickerElement.class)
                .id("picker-with-valid-range");
        assertValidStateOfPickerWithValidRange(valid);

        element.setDateTime(invalidValue);
        assertValidStateOfPickerWithValidRange(!valid);

        // Forcing min or max to invalid value on the client does not make the
        // field valid
        element.setProperty(clientPropertyUnderTest, invalidValue.toString());
        getCommandExecutor().waitForVaadin();
        assertValidStateOfPickerWithValidRange(!valid);

        // Forcing the field to be valid does not work
        element.setProperty("invalid", false);
        getCommandExecutor().waitForVaadin();
        assertValidStateOfPickerWithValidRange(!valid);

        // Setting a valid value makes the field return to valid mode
        element.setDateTime(validValue);
        getCommandExecutor().waitForVaadin();
        assertValidStateOfPickerWithValidRange(valid);
    }

    private void assertValidStateOfPickerWithValidRange(boolean valid) {
        final TestBenchElement checkIsInvalid = $("button")
                .id("check-is-invalid");
        checkIsInvalid.click();

        final String expectedValue = !valid ? "invalid" : "valid";
        Assert.assertEquals(expectedValue, $("div").id("is-invalid").getText());
    }

}
