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
package com.vaadin.flow.component.checkbox.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.HasValidationProperties;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.tests.MockUIExtension;

class CheckboxUnitTest {
    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    @Test
    void initialValue() {
        Checkbox checkbox = new Checkbox();
        Assertions.assertFalse(checkbox.getValue());

        checkbox = new Checkbox(true);
        Assertions.assertTrue(checkbox.getValue());

        checkbox = new Checkbox(false);
        Assertions.assertFalse(checkbox.getValue());
    }

    @Test
    void testIndeterminate() {
        Checkbox checkbox = new Checkbox();
        Assertions.assertFalse(checkbox.isIndeterminate());

        checkbox = new Checkbox(true);
        Assertions.assertFalse(checkbox.isIndeterminate());

        checkbox.setIndeterminate(true);
        Assertions.assertTrue(checkbox.getValue());
        Assertions.assertTrue(checkbox.isIndeterminate());

        checkbox.setValue(true);
        Assertions.assertTrue(checkbox.getValue());
        Assertions.assertTrue(checkbox.isIndeterminate());

        checkbox.setValue(false);
        Assertions.assertFalse(checkbox.getValue());
        Assertions.assertTrue(checkbox.isIndeterminate());

        checkbox.setIndeterminate(false);
        Assertions.assertFalse(checkbox.getValue());
        Assertions.assertFalse(checkbox.isIndeterminate());
    }

    @Test
    void labelAndInitialValueCtor() {
        Checkbox checkbox = new Checkbox("foo", true);
        Assertions.assertTrue(checkbox.getValue());
        Assertions.assertEquals("foo", checkbox.getLabel());

        checkbox = new Checkbox("foo", false);
        Assertions.assertFalse(checkbox.getValue());
        Assertions.assertEquals("foo", checkbox.getLabel());
    }

    @Test
    void setEnable() {
        Checkbox checkbox = new Checkbox("foo", true);
        checkbox.setEnabled(true);
        Assertions.assertTrue(checkbox.isEnabled());
        checkbox.setEnabled(false);
        Assertions.assertFalse(checkbox.isEnabled());
    }

    @Test
    void elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue() {
        Element element = new Element("vaadin-checkbox");
        element.setProperty("checked", true);

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(ui.getService().getInstantiator())
                .thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(Checkbox.class))
                .thenAnswer(invocation -> new Checkbox());
        Checkbox field = Component.from(element, Checkbox.class);
        Assertions.assertEquals(Boolean.TRUE,
                field.getElement().getPropertyRaw("checked"));
    }

    @Test
    void implementsHasTooltip() {
        Checkbox checkbox = new Checkbox();
        Assertions.assertTrue(checkbox instanceof HasTooltip);
    }

    @Test
    void implementHasAriaLabel() {
        Checkbox checkbox = new Checkbox();
        Assertions.assertTrue(checkbox instanceof HasAriaLabel);
    }

    @Test
    void setAriaLabel() {
        Checkbox checkbox = new Checkbox();
        checkbox.setAriaLabel("aria-label");

        Assertions.assertTrue(checkbox.getAriaLabel().isPresent());
        Assertions.assertEquals("aria-label", checkbox.getAriaLabel().get());

        checkbox.setAriaLabel(null);
        Assertions.assertTrue(checkbox.getAriaLabel().isEmpty());
    }

    @Test
    void setAriaLabelledBy() {
        Checkbox checkbox = new Checkbox();
        checkbox.setAriaLabelledBy("aria-labelledby");

        Assertions.assertTrue(checkbox.getAriaLabelledBy().isPresent());
        Assertions.assertEquals("aria-labelledby",
                checkbox.getAriaLabelledBy().get());

        checkbox.setAriaLabelledBy(null);
        Assertions.assertTrue(checkbox.getAriaLabelledBy().isEmpty());
    }

    @Test
    void implementsInputField() {
        Checkbox field = new Checkbox();
        Assertions.assertTrue(
                field instanceof InputField<AbstractField.ComponentValueChangeEvent<Checkbox, Boolean>, Boolean>);
    }

    @Test
    void implementsHasValidationProperties() {
        Checkbox field = new Checkbox();
        Assertions.assertTrue(field instanceof HasValidationProperties);
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions.assertTrue(
                HasThemeVariant.class.isAssignableFrom(Checkbox.class));
    }
}
