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

import com.vaadin.flow.component.checkbox.Switch;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.tests.MockUIExtension;

class SwitchTest {
    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    @Test
    void initialValue() {
        Switch field = new Switch();
        Assertions.assertFalse(field.getValue());

        field = new Switch(true);
        Assertions.assertTrue(field.getValue());

        field = new Switch(false);
        Assertions.assertFalse(field.getValue());
    }

    @Test
    void setValue_reflectedInCheckedProperty() {
        Switch field = new Switch();
        Assertions
                .assertFalse(field.getElement().getProperty("checked", false));

        field.setValue(true);
        Assertions.assertTrue(field.getElement().getProperty("checked", false));

        field.setValue(false);
        Assertions
                .assertFalse(field.getElement().getProperty("checked", false));
    }

    @Test
    void setLabel() {
        Switch field = new Switch();
        Assertions.assertNull(field.getLabel());

        field.setLabel("Notifications");
        Assertions.assertEquals("Notifications", field.getLabel());
    }

    @Test
    void labelConstructor() {
        Switch field = new Switch("Notifications");
        Assertions.assertEquals("Notifications", field.getLabel());
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions.assertTrue(
                HasThemeVariant.class.isAssignableFrom(Switch.class));
    }
}
