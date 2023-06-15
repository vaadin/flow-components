/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.datepicker.validation;

import java.time.LocalDate;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.shared.HasClientValidation.ClientValidatedEvent;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

public class BasicValidationTest
        extends AbstractBasicValidationTest<DatePicker> {
    protected DatePicker createTestField() {
        return new DatePicker();
    }

    @Test
    public void setInternalValidationDisabled_changeValue_noValidation() {
        testField.setRequired(true);
        testField.setInternalValidationDisabled(true);

        testField.setValue(LocalDate.now());
        Assert.assertFalse(testField.isInvalid());

        testField.setValue(null);
        Assert.assertFalse(testField.isInvalid());
    }

    @Test
    public void setInternalValidationDisabled_fireClientValidatedEvent_noValidation() {
        testField.setRequired(true);
        testField.setInternalValidationDisabled(true);

        ComponentUtil.fireEvent(testField, new ClientValidatedEvent(testField, false));
        Assert.assertFalse(testField.isInvalid());
    }
}
