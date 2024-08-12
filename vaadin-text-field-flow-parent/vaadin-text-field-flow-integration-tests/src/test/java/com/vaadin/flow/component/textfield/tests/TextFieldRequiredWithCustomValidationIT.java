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
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-text-field/required-field-custom-validator")
public class TextFieldRequiredWithCustomValidationIT
        extends AbstractComponentIT {

    @Test
    public void requiredAndCustomValidationOnServerSide_initialStateIsInvalid_changingToValidValueResetsInvalidFlag()
            throws Exception {
        open();

        TestBenchElement textField = $("vaadin-text-field").first();
        TestBenchElement input = textField.$("input").first();

        Assert.assertEquals(Boolean.TRUE.toString(),
                textField.getAttribute("invalid"));
        Assert.assertEquals("invalid", textField.getAttribute("value"));

        while (!input.getAttribute("value").isEmpty()) {
            input.sendKeys(Keys.BACK_SPACE);
        }
        Assert.assertEquals("", textField.getAttribute("value"));

        input.sendKeys("Valid");
        input.sendKeys(Keys.ENTER);

        Assert.assertEquals("Valid", textField.getAttribute("value"));
        Assert.assertEquals(Boolean.FALSE.toString(),
                textField.getAttribute("invalid"));
    }
}
