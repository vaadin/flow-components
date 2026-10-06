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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.shared.HasTextSelection.TextSelectionJs;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.StateNode;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.flow.js.JsCall;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.node.ObjectNode;

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

    @Test
    void deselect_collapsesSelection() {
        component.deselect();

        List<JsCall> calls = dumpTextSelectionCalls();
        Assertions.assertEquals(1, calls.size());
        Assertions.assertEquals("deselect", calls.get(0).methodName());
        Assertions.assertEquals(List.of(), calls.get(0).arguments());
    }

    @Test
    void selectionSignal_initialValueIsEmpty() {
        Assertions.assertEquals(SelectionRange.empty(),
                component.selectionSignal().peek());
    }

    @Test
    void selectionSignal_returnsSameReadonlyInstance() {
        Signal<SelectionRange> signal = component.selectionSignal();

        Assertions.assertSame(signal, component.selectionSignal());
        Assertions.assertFalse(signal instanceof ValueSignal);
    }

    @Test
    void selectionSignal_detached_installsSelectionListenerOnAttach() {
        ui.remove(component);
        component.selectionSignal();
        assertInstallSelectionListenerCalls(0);

        ui.add(component);
        assertInstallSelectionListenerCalls(1);
    }

    @Test
    void selectionSignal_reattach_reinstallsSelectionListener() {
        component.selectionSignal();
        dumpTextSelectionCalls();

        ui.remove(component);
        ui.add(component);

        assertInstallSelectionListenerCalls(1);
    }

    @Test
    void selectionSignal_multipleCalls_installsSelectionListenerOnce() {
        component.selectionSignal();
        component.selectionSignal();

        assertInstallSelectionListenerCalls(1);
    }

    @Test
    void selectionChangeEvent_updatesSelectionSignal() {
        Signal<SelectionRange> signal = component.selectionSignal();

        fireSelectionChange(6, 11, "world");
        Assertions.assertEquals(new SelectionRange(6, 11, "world"),
                signal.peek());

        fireSelectionChange(4, 4, "");
        Assertions.assertEquals(new SelectionRange(4, 4, ""), signal.peek());
    }

    @Test
    void selectionChangeEvent_reattach_updatesSelectionSignal() {
        Signal<SelectionRange> signal = component.selectionSignal();
        ui.remove(component);
        ui.add(component);

        fireSelectionChange(0, 3, "Hel");

        Assertions.assertEquals(new SelectionRange(0, 3, "Hel"), signal.peek());
    }

    @Test
    void selectionChangeEvent_invalidRange_ignored() {
        Signal<SelectionRange> signal = component.selectionSignal();
        fireSelectionChange(1, 4, "bcd");

        fireSelectionChange(-1, 4, "abcd");
        fireSelectionChange(5, 2, "");
        fireSelectionChange(0, 2, "abc");

        Assertions.assertEquals(new SelectionRange(1, 4, "bcd"), signal.peek());
    }

    @Test
    void selectionChangeEvent_malformedData_ignored() {
        Signal<SelectionRange> signal = component.selectionSignal();
        fireSelectionChange(1, 4, "bcd");

        ObjectNode missingContent = JacksonUtils.createObjectNode();
        missingContent.put("event.detail.start", 0);
        missingContent.put("event.detail.end", 1);
        fireSelectionChange(missingContent);

        ObjectNode textStart = JacksonUtils.createObjectNode();
        textStart.put("event.detail.start", "0");
        textStart.put("event.detail.end", 1);
        textStart.put("event.detail.content", "a");
        fireSelectionChange(textStart);

        Assertions.assertEquals(new SelectionRange(1, 4, "bcd"), signal.peek());
    }

    @Test
    void selectionChangeEvent_rerunsEffect() {
        List<SelectionRange> values = new ArrayList<>();
        Signal.effect(component,
                () -> values.add(component.selectionSignal().get()));

        fireSelectionChange(6, 11, "world");

        Assertions.assertEquals(List.of(SelectionRange.empty(),
                new SelectionRange(6, 11, "world")), values);
    }

    @Test
    void selectionSignal_elementWithoutComponent_throws() {
        Element element = new Element("input");
        HasTextSelection hasTextSelection = new HasTextSelection() {
            @Override
            public Element getElement() {
                return element;
            }
        };

        Assertions.assertThrows(IllegalStateException.class,
                hasTextSelection::selectionSignal);
    }

    private void fireSelectionChange(int start, int end, String content) {
        ObjectNode data = JacksonUtils.createObjectNode();
        data.put("event.detail.start", start);
        data.put("event.detail.end", end);
        data.put("event.detail.content", content);
        fireSelectionChange(data);
    }

    private void fireSelectionChange(ObjectNode data) {
        component.getElement().getNode().getFeature(ElementListenerMap.class)
                .fireEvent(new DomEvent(component.getElement(),
                        "vaadin-text-selection-change", data));
    }

    private void assertInstallSelectionListenerCalls(int expected) {
        long count = dumpTextSelectionCalls().stream().filter(
                call -> call.methodName().equals("installSelectionListener"))
                .count();
        Assertions.assertEquals(expected, count);
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
