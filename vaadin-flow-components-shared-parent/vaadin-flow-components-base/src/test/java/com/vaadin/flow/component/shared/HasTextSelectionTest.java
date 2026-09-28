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
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.shared.HasTextSelection.TextSelectionJs;
import com.vaadin.flow.internal.StateNode;
import com.vaadin.flow.js.JsCall;
import com.vaadin.tests.MockUIExtension;

class HasTextSelectionTest {

    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    @Tag("test")
    private static class TestComponent extends Component
            implements HasTextSelection {
    }

    private TestComponent component;

    @BeforeEach
    void setup() {
        component = new TestComponent();
        ui.add(component);
        ui.fakeClientCommunication();
    }

    @Test
    void setSelectionRange_setsRange() {
        component.setSelectionRange(2, 7);

        assertSelectionRangeCall(2, 7);
    }

    @Test
    void setSelectionRange_sameStartAndEnd_setsCollapsedRange() {
        component.setSelectionRange(3, 3);

        assertSelectionRangeCall(3, 3);
    }

    @Test
    void selectAll_setsRangeFromStartToMaxValue() {
        component.selectAll();

        assertSelectionRangeCall(0, Integer.MAX_VALUE);
    }

    @Test
    void setCursorPosition_setsCollapsedRangeAtPosition() {
        component.setCursorPosition(4);

        assertSelectionRangeCall(4, 4);
    }

    @Test
    void multipleCalls_schedulesEachCall() {
        component.setSelectionRange(2, 7);
        component.setCursorPosition(4);

        List<JsCall> calls = dumpTextSelectionCalls();
        Assertions.assertEquals(2, calls.size());
        Assertions.assertEquals(List.of(2, 7), calls.get(0).arguments());
        Assertions.assertEquals(List.of(4, 4), calls.get(1).arguments());
    }

    private void assertSelectionRangeCall(int selectionStart,
            int selectionEnd) {
        List<JsCall> calls = dumpTextSelectionCalls();
        Assertions.assertEquals(1, calls.size());
        JsCall call = calls.get(0);
        Assertions.assertEquals("setSelectionRange", call.methodName());
        Assertions.assertEquals(List.of(selectionStart, selectionEnd),
                call.arguments());
    }

    private List<JsCall> dumpTextSelectionCalls() {
        StateNode node = component.getElement().getNode();
        return ui.dumpPendingJavaScriptInvocations().stream()
                .filter(pending -> pending.getOwner() == node)
                .map(pending -> pending.getInvocation().getJsCall())
                .filter(Objects::nonNull)
                .filter(call -> call.definitionType() == TextSelectionJs.class)
                .toList();
    }
}
