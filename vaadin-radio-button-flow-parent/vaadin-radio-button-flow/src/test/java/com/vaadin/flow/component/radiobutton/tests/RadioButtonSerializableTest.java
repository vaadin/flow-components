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
package com.vaadin.flow.component.radiobutton.tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.testutil.ClassesSerializableTest;
import com.vaadin.tests.MockUIExtension;

class RadioButtonSerializableTest extends ClassesSerializableTest {
    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    @Test
    void setItems_addToUI_radioButtonGroupIsSerializable() throws Throwable {
        var group = new RadioButtonGroup<>();
        group.setItems("Item 1", "Item 2");

        // Serializing session requires more setup, not necessary for this test
        ui.getUI().getInternals().setSession(null);
        ui.add(group);

        serializeAndDeserialize(ui.getUI());
    }
}
