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
package com.vaadin.flow.component.textfield.tests.validation;

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-text-field/validation/constraints")
public class TextFieldValidationConstraintsIT
        extends AbstractValidationConstraintsIT<TextFieldElement> {
    @Test
    public void minLength() {
        $("input").id("min-length").sendKeys("2", Keys.ENTER);

        field.setValue("A");
        assertClientValid(false);
        assertServerValid(false);

        field.setValue("AA");
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void maxLength() {
        $("input").id("max-length").sendKeys("1", Keys.ENTER);

        field.setValue("AA");
        assertClientValid(false);
        assertServerValid(false);

        field.setValue("A");
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void pattern() {
        $("input").id("pattern").sendKeys("^\\d+$", Keys.ENTER);

        field.setValue("Word");
        assertClientValid(false);
        assertServerValid(false);

        field.setValue("1234");
        assertClientValid(true);
        assertServerValid(true);
    }

    protected TextFieldElement getField() {
        return $(TextFieldElement.class).first();
    }
}
