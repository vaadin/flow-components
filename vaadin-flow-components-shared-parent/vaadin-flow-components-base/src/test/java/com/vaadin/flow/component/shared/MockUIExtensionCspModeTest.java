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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.js.JsCall;
import com.vaadin.flow.server.CspMode;
import com.vaadin.flow.server.CspViolationException;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.tests.MockUIExtension;

/**
 * Covers {@link MockUIExtension#setCspMode(CspMode)}, with a component that
 * runs declared JavaScript through {@link ValidationJs}.
 */
class MockUIExtensionCspModeTest {

    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    @Test
    void noCspMode_stringJavaScript_runs() {
        TestComponent component = new TestComponent();
        ui.add(component);

        Assertions.assertDoesNotThrow(
                () -> component.getElement().executeJs("this.foo = 1"));
    }

    @Test
    void strictCspMode_stringJavaScript_throws() {
        ui.setCspMode(CspMode.STRICT);
        TestComponent component = new TestComponent();
        ui.add(component);

        Assertions.assertThrows(CspViolationException.class,
                () -> component.getElement().executeJs("this.foo = 1"));
    }

    @Test
    void strictCspMode_declaredJavaScript_runs() {
        ui.setCspMode(CspMode.STRICT);
        TestComponent component = new TestComponent();
        ui.add(component);

        component.setInvalid(true);

        List<JsCall> calls = ui.dumpJsCalls(component.getElement(),
                ValidationJs.class);
        Assertions.assertEquals(1, calls.size());
        Assertions.assertEquals("setInvalid", calls.get(0).methodName());
    }

    @Test
    void setCspMode_tearDown_clearsCurrentService() {
        ui.setCspMode(CspMode.STRICT);
        Assertions.assertSame(ui.getService(), VaadinService.getCurrent());

        ui.tearDown();

        Assertions.assertNull(VaadinService.getCurrent());
    }

    @Tag("test-component")
    private static class TestComponent extends Component
            implements HasValidationProperties {
    }
}
