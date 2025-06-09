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
package com.vaadin.flow.component.customfield.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.customfield.testbench.CustomFieldElement;
import com.vaadin.flow.component.textfield.testbench.IntegerFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-custom-field/validation")
public class ValidationIT extends AbstractComponentIT {

    private CustomFieldElement customField;
    private TestBenchElement logOutput;

    private CustomFieldElement customFieldWithDelegatedValidation;

    @Before
    public void init() {
        open();

        customField = $(CustomFieldElement.class).id("custom-field");
        logOutput = $("span").id("log-output");
        customFieldWithDelegatedValidation = $(CustomFieldElement.class)
                .id("custom-field-with-delegated-validation");
    }

    @Test
    public void overridesClientValidation() {
        clickElementWithJs("set-invalid");

        executeScript("arguments[0]._requestValidation()", customField);

        Assert.assertEquals(true, customField.getPropertyBoolean("invalid"));
    }

    @Test
    public void detach_reattach_overridesClientValidation() {
        clickElementWithJs("set-invalid");
        clickElementWithJs("detach");
        clickElementWithJs("attach");

        customField = $(CustomFieldElement.class).id("custom-field");
        executeScript("arguments[0]._requestValidation()", customField);

        Assert.assertEquals(true, customField.getPropertyBoolean("invalid"));
    }

    @Test
    public void changeInvalidOnClient_notSynchronizedToServer() {
        clickElementWithJs("set-invalid");

        executeScript("arguments[0].invalid = false", customField);
        clickElementWithJs("log-invalid-state");

        Assert.assertEquals("true", logOutput.getText());
    }

    @Test
    public void delegatedValidation_initiallyInvalid_focus_blur_noClientValidation() {
        clickElementWithJs("validate");
        customFieldWithDelegatedValidation.focus();
        customFieldWithDelegatedValidation.sendKeys(Keys.TAB);

        var innerField = customFieldWithDelegatedValidation
                .$(IntegerFieldElement.class).first();

        Assert.assertTrue(innerField.getPropertyBoolean("invalid"));
    }
}
