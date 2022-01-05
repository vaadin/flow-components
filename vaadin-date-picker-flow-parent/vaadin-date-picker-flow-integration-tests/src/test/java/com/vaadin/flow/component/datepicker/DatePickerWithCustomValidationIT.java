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
package com.vaadin.flow.component.datepicker;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.Keys;

@TestPath("vaadin-date-picker/required-field-custom-validator")
public class DatePickerWithCustomValidationIT extends AbstractComponentIT {

    @Test
    public void requiredAndCustomValidationOnServerSide_initialStateIsInvalid_changingToValidValueResetsInvalidFlag()
            throws Exception {
        open();

        TestBenchElement dateField = $("vaadin-date-picker").first();
        TestBenchElement input = dateField.$("input").first();

        Assert.assertEquals(Boolean.TRUE.toString(),
                dateField.getAttribute("invalid"));
        Assert.assertEquals("2019-01-02", dateField.getAttribute("value"));

        while (!input.getAttribute("value").isEmpty()) {
            input.sendKeys(Keys.BACK_SPACE);
        }
        input.sendKeys(Keys.ENTER);
        Assert.assertEquals("", dateField.getAttribute("value"));

        input.sendKeys("01/01/2019");
        input.sendKeys(Keys.ENTER);

        Assert.assertEquals("2019-01-01", dateField.getAttribute("value"));
        Assert.assertEquals(Boolean.FALSE.toString(),
                dateField.getAttribute("invalid"));
    }
}
