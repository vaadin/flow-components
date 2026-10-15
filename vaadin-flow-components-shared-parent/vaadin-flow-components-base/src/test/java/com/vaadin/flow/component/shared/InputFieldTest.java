/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.shared;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.function.SerializableFunction;

public class InputFieldTest {

    private TestComponent component;

    @Before
    public void setup() {
        component = new TestComponent();
    }

    @Test
    public void checkExtendedInterfaces() {
        Assert.assertTrue(component instanceof HasEnabled);
        Assert.assertTrue(component instanceof HasLabel);
        Assert.assertTrue(component instanceof HasSize);
        Assert.assertTrue(component instanceof HasStyle);
        Assert.assertTrue(component instanceof HasTooltip);
        Assert.assertTrue(component instanceof HasValue);
    }

    @Tag("test")
    private static class TestComponent extends
            AbstractSinglePropertyField<TestComponent, String> implements
            InputField<AbstractField.ComponentValueChangeEvent<TestComponent, String>, String>,
            HasComponents {

        private static final SerializableFunction<String, String> PARSER = valueFromClient -> {
            return valueFromClient;
        };

        private static final SerializableFunction<String, String> FORMATTER = valueFromModel -> {
            return valueFromModel;
        };

        public TestComponent() {
            super("value", "", String.class, PARSER, FORMATTER);

        }

    }
}
