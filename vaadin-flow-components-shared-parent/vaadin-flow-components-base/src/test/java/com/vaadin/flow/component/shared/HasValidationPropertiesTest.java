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

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.js.JsCall;
import com.vaadin.tests.MockUIExtension;

class HasValidationPropertiesTest {

    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    private TestComponent component;

    @BeforeEach
    void setup() {
        component = new TestComponent();
    }

    @Test
    void initialErrorMessage() {
        Assertions.assertEquals(null, component.getErrorMessage());
    }

    @Test
    void changeErrorMessage() {
        component.setErrorMessage("This field is required");
        Assertions.assertEquals("This field is required",
                component.getElement().getProperty("errorMessage"));

        component.setErrorMessage(null);
        Assertions.assertEquals("",
                component.getElement().getProperty("errorMessage"));
    }

    @Test
    void initialInvalid() {
        Assertions.assertFalse(component.isInvalid());
    }

    @Test
    void changeInvalid() {
        component.setInvalid(true);
        Assertions.assertTrue(component.isInvalid());
        Assertions.assertTrue(
                component.getElement().getProperty("invalid", false));

        component.setInvalid(false);
        Assertions.assertFalse(component.isInvalid());
        Assertions.assertFalse(
                component.getElement().getProperty("invalid", false));
    }

    @Test
    void setInvalid_setsInvalidPropertyOnClient() {
        ui.add(component);
        ui.fakeClientCommunication();

        component.setInvalid(true);
        component.setInvalid(false);

        List<JsCall> calls = ui.dumpJsCalls(component.getElement(),
                ValidationJs.class);
        Assertions.assertEquals(2, calls.size());
        Assertions.assertEquals("setInvalid", calls.get(0).methodName());
        Assertions.assertEquals(List.of(true), calls.get(0).arguments());
        Assertions.assertEquals("setInvalid", calls.get(1).methodName());
        Assertions.assertEquals(List.of(false), calls.get(1).arguments());
    }

    @Tag("test")
    private static class TestComponent extends Component
            implements HasValidationProperties {
    }
}
