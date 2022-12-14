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
package com.vaadin.flow.component.textfield.validation;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.tests.validation.HasValidationTestHelper;

public class NumberFieldBasicValidationTest {
    private NumberField testField;

    @Before
    public void setup() {
        testField = new NumberField();
    }

    @Test
    public void setErrorMessage_getErrorMessage() {
        HasValidationTestHelper.setErrorMessage_getErrorMessage(testField);
    }

    @Test
    public void setInvalid_isInvalid() {
        HasValidationTestHelper.setInvalid_isInvalid(testField);
    }
}
