/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.tests.validation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.shared.HasClientValidation.ClientValidatedEvent;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;

import elemental.json.Json;

/**
 * An abstract class that provides tests verifying that a component correctly
 * implements the {@link HasValidation} interface.
 */
public abstract class AbstractBasicValidationTest<C extends AbstractField<C, V> & HasValidation, V> {
    protected C testField;

    @Before
    public void setup() {
        testField = createTestField();
    }

    @Test
    public void setRequired_setManualValidation_fireValueChangeEvent_noValidation() {
        testField.setRequiredIndicatorVisible(true);
        testField.setManualValidation(true);

        ComponentUtil.fireEvent(testField, new ComponentValueChangeEvent<>(
                testField, testField, testField.getEmptyValue(), false));
        Assert.assertFalse(testField.isInvalid());
    }

    @Test
    public void setRequired_setManualValidation_fireUnparsableChangeEvent_noValidation() {
        testField.setRequiredIndicatorVisible(true);
        testField.setManualValidation(true);

        fireUnparsableChangeDomEvent();
        Assert.assertFalse(testField.isInvalid());
    }

    @Test
    public void setRequired_setManualValidation_fireClientValidatedEvent_noValidation() {
        testField.setRequiredIndicatorVisible(true);
        testField.setManualValidation(true);

        ComponentUtil.fireEvent(testField,
                new ClientValidatedEvent(testField, false));
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

    protected abstract C createTestField();

    private void fireUnparsableChangeDomEvent() {
        DomEvent unparsableChangeDomEvent = new DomEvent(testField.getElement(),
                "unparsable-change", Json.createObject());
        testField.getElement().getNode().getFeature(ElementListenerMap.class)
                .fireEvent(unparsableChangeDomEvent);
    }
}
