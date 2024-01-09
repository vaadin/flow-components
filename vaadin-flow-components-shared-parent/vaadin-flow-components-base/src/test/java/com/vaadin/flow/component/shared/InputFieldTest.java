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
package com.vaadin.flow.component.shared;

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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
