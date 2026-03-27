/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;

/**
 * An abstract class that provides tests verifying that a component correctly
 * implements the {@link HasValidation} interface.
 */
public abstract class AbstractBasicValidationTest<C extends AbstractField<C, V> & HasValidation, V> {
    protected C testField;

    @BeforeEach
    void setup() {
        testField = createTestField();
    }

    @Test
    void webComponentManualValidationEnabled() {
        Assertions.assertTrue(
                testField.getElement().getProperty("manualValidation", false));
    }

    @Test
    void setRequired_setManualValidation_fireValueChangeEvent_noValidation() {
        testField.setRequiredIndicatorVisible(true);
        testField.setManualValidation(true);

        ComponentUtil.fireEvent(testField, new ComponentValueChangeEvent<>(
                testField, testField, testField.getEmptyValue(), false));
        Assertions.assertFalse(testField.isInvalid());
    }

    @Test
    void setRequired_setManualValidation_fireUnparsableChangeEvent_noValidation() {
        testField.setRequiredIndicatorVisible(true);
        testField.setManualValidation(true);

        fireUnparsableChangeDomEvent();
        Assertions.assertFalse(testField.isInvalid());
    }

    @Test
    void setErrorMessage_getErrorMessage() {
        Assertions.assertNull(testField.getErrorMessage());
        Assertions
                .assertNull(testField.getElement().getProperty("errorMessage"));

        testField.setErrorMessage("Error");

        Assertions.assertEquals("Error", testField.getErrorMessage());
        Assertions.assertEquals("Error",
                testField.getElement().getProperty("errorMessage"));
    }

    @Test
    void setInvalid_isInvalid() {
        Assertions.assertFalse(testField.isInvalid());
        Assertions.assertFalse(
                testField.getElement().getProperty("invalid", false));

        testField.setInvalid(true);

        Assertions.assertTrue(testField.isInvalid());
        Assertions.assertTrue(
                testField.getElement().getProperty("invalid", false));
    }

    protected abstract C createTestField();

    private void fireUnparsableChangeDomEvent() {
        DomEvent unparsableChangeDomEvent = new DomEvent(testField.getElement(),
                "unparsable-change", JacksonUtils.createObjectNode());
        testField.getElement().getNode().getFeature(ElementListenerMap.class)
                .fireEvent(unparsableChangeDomEvent);
    }
}
