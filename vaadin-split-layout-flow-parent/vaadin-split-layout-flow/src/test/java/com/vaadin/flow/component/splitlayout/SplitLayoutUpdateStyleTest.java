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
package com.vaadin.flow.component.splitlayout;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.js.JsCall;
import com.vaadin.tests.MockUIExtension;

class SplitLayoutUpdateStyleTest {

    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    private SplitLayout splitLayout;

    @BeforeEach
    void setup() {
        splitLayout = new SplitLayout();
        ui.add(splitLayout);
        ui.dumpPendingJavaScriptInvocations();
    }

    @Test
    void noComponents_setSplitterPosition_setsStyleOfChildren() {
        splitLayout.setSplitterPosition(30);

        List<JsCall> calls = ui.dumpJsCalls(splitLayout.getElement(),
                SplitLayoutJs.class);

        Assertions.assertEquals(2, calls.size());
        assertSetChildStyleCall(calls.get(0), 0, "1 1 30.0%");
        assertSetChildStyleCall(calls.get(1), 1, "1 1 70.0%");
    }

    @Test
    void withComponents_setSplitterPosition_setsStyleOfComponents() {
        Div primary = new Div();
        Div secondary = new Div();
        splitLayout.addToPrimary(primary);
        splitLayout.addToSecondary(secondary);
        ui.dumpPendingJavaScriptInvocations();

        splitLayout.setSplitterPosition(30);

        Assertions.assertTrue(
                ui.dumpJsCalls(splitLayout.getElement(), SplitLayoutJs.class)
                        .isEmpty());
        Assertions.assertEquals("1 1 30.0%", primary.getStyle().get("flex"));
        Assertions.assertEquals("1 1 70.0%", secondary.getStyle().get("flex"));
    }

    private void assertSetChildStyleCall(JsCall call, int index, String flex) {
        Assertions.assertEquals("setChildStyle", call.methodName());
        Assertions.assertEquals(List.of(index, "flex", flex), call.arguments());
    }
}
