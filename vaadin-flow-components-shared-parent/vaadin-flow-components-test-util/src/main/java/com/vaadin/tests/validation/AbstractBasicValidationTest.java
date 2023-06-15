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
package com.vaadin.tests.validation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.shared.HasClientValidation.ClientValidatedEvent;

/**
 * An abstract class that provides tests verifying that a component correctly
 * implements the {@link HasValidation} interface.
 */
public abstract class AbstractBasicValidationTest<T extends AbstractField<T, V> & HasValidation, V> {
    protected T testField;

    @Before
    public void setup() {
        testField = createTestField();
    }

    @Test
    public void setInternalValidationDisabled_isInternalValidationDisabled() {
        Assert.assertFalse(testField.isInternalValidationDisabled());

        testField.setInternalValidationDisabled(true);

        Assert.assertTrue(testField.isInternalValidationDisabled());
    }

    @Test
    public void setRequired_setInternalValidationDisabled_fireValueChangeEvent_noValidation() {
        testField.setRequiredIndicatorVisible(true);
        testField.setInternalValidationDisabled(true);

        ComponentUtil.fireEvent(testField, new ComponentValueChangeEvent<>(testField, testField, testField.getEmptyValue(), false));
        Assert.assertFalse(testField.isInvalid());
    }

    @Test
    public void setRequired_setInternalValidationDisabled_fireClientValidatedEvent_noValidation() {
        testField.setRequiredIndicatorVisible(true);
        testField.setInternalValidationDisabled(true);

        ComponentUtil.fireEvent(testField, new ClientValidatedEvent(testField, false));
        Assert.assertFalse(testField.isInvalid());
    }

    @Test
    public void setErrorMessage_getErrorMessage() {
        Assert.assertNull(testField.getErrorMessage());
        Assert.assertNull(testField.getElement().getProperty("errorMessage"));

        testField.setErrorMessage("Error");

        Assert.assertEquals("Error", testField.getErrorMessage());
        Assert.assertEquals("Error",
                testField.getElement().getProperty("errorMessage"));
    }

    @Test
    public void setInvalid_isInvalid() {
        Assert.assertFalse(testField.isInvalid());
        Assert.assertFalse(
                testField.getElement().getProperty("invalid", false));

        testField.setInvalid(true);

        Assert.assertTrue(testField.isInvalid());
        Assert.assertTrue(testField.getElement().getProperty("invalid", false));
    }

    protected abstract T createTestField();
}
