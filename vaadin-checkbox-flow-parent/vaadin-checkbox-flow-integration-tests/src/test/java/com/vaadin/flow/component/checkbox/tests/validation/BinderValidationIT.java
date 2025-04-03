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
package com.vaadin.flow.component.checkbox.tests.validation;

import static com.vaadin.flow.component.checkbox.tests.validation.BinderValidationPage.REQUIRED_ERROR_MESSAGE;

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.component.checkbox.testbench.CheckboxGroupElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

@TestPath("vaadin-checkbox-group/validation/binder")
public class BinderValidationIT
        extends AbstractValidationIT<CheckboxGroupElement> {

    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
        assertErrorMessage(null);
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        testField.$(CheckboxElement.class).last().sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void required_changeValue_assertValidity() {
        testField.selectByText("foo");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();

        testField.deselectByText("foo");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Override
    protected CheckboxGroupElement getTestField() {
        return $(CheckboxGroupElement.class).first();
    }
}
