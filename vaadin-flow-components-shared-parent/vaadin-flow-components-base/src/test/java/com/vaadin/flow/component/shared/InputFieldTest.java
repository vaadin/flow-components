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
package com.vaadin.flow.component.shared;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

class InputFieldTest {

    private TestComponent component;

    @BeforeEach
    void setup() {
        component = new TestComponent();
    }

    @Test
    void checkExtendedInterfaces() {
        Assertions.assertTrue(component instanceof HasEnabled);
        Assertions.assertTrue(component instanceof HasLabel);
        Assertions.assertTrue(component instanceof HasSize);
        Assertions.assertTrue(component instanceof HasStyle);
        Assertions.assertTrue(component instanceof HasTooltip);
        Assertions.assertTrue(component instanceof HasValue);
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
