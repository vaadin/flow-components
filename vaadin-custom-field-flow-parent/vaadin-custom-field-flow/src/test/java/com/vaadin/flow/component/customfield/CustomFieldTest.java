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
package com.vaadin.flow.component.customfield;

import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.InputField;

@SuppressWarnings("unchecked")
class CustomFieldTest {

    private final Object value = new Object();

    private CustomField<Object> systemUnderTest;

    private Consumer<Object> consumer;

    @BeforeEach
    void setup() {
        consumer = Mockito.mock(Consumer.class);
        systemUnderTest = new CustomField<Object>() {
            @Override
            protected Object generateModelValue() {
                return value;
            }

            @Override
            protected void setPresentationValue(Object newPresentationValue) {
                consumer.accept(newPresentationValue);
            }
        };
    }

    @Test
    void valuesAreUpdated() {
        Assertions.assertNull(systemUnderTest.getValue());
        systemUnderTest.setValue(value);
        Assertions.assertSame(value, systemUnderTest.getValue());
        Mockito.verify(consumer).accept(value);
        Consumer<Object> listener = Mockito.mock(Consumer.class);
        systemUnderTest
                .addValueChangeListener(e -> listener.accept(e.getValue()));
    }

    @Test
    void implementsHasTooltip() {
        Assertions.assertTrue(systemUnderTest instanceof HasTooltip);
    }

    @Test
    void implementsInputField() {
        Assertions.assertTrue(
                systemUnderTest instanceof InputField<AbstractField.ComponentValueChangeEvent<CustomField<Object>, Object>, Object>);
    }
}
