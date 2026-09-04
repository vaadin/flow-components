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
package com.vaadin.flow.component.textfield.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.shared.SelectionRange;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.flow.signals.Signal;
import com.vaadin.tests.AbstractSignalsTest;

import tools.jackson.databind.node.ObjectNode;

class SelectionSignalTest extends AbstractSignalsTest {

    private TextField textField;

    @BeforeEach
    void setup() {
        textField = new TextField();
    }

    @Test
    void initialValue_isEmpty() {
        Signal<SelectionRange> signal = textField.selectionSignal();
        Assertions.assertEquals(SelectionRange.empty(), signal.peek());
    }

    @Test
    void sameInstance_returnedAcrossCalls() {
        Signal<SelectionRange> first = textField.selectionSignal();
        Signal<SelectionRange> second = textField.selectionSignal();
        Assertions.assertSame(first, second);
    }

    @Test
    void clientSelectionEvent_updatesSignal() {
        UI.getCurrent().add(textField);
        textField.setValue("Hello world");
        Signal<SelectionRange> signal = textField.selectionSignal();

        fireSelectionChange(textField.getElement(), 6, 11, "world");

        SelectionRange current = signal.peek();
        Assertions.assertEquals(6, current.start());
        Assertions.assertEquals(11, current.end());
        Assertions.assertEquals("world", current.content());
        Assertions.assertEquals(5, current.length());
        Assertions.assertFalse(current.isEmpty());
    }

    @Test
    void collapsedSelection_signalReportsEmptyRange() {
        UI.getCurrent().add(textField);
        Signal<SelectionRange> signal = textField.selectionSignal();

        fireSelectionChange(textField.getElement(), 4, 4, "");

        Assertions.assertTrue(signal.peek().isEmpty());
        Assertions.assertEquals(4, signal.peek().start());
    }

    @Test
    void detachedField_eventListenerStillRegistered() {
        // Signal is established while detached; later attachment + event
        // should still flow through to the signal.
        Signal<SelectionRange> signal = textField.selectionSignal();
        UI.getCurrent().add(textField);
        textField.setValue("abcdef");

        fireSelectionChange(textField.getElement(), 1, 4, "bcd");

        Assertions.assertEquals(new SelectionRange(1, 4, "bcd"), signal.peek());
    }

    @Test
    void signalSurvivesDetachAndReattach() {
        UI.getCurrent().add(textField);
        Signal<SelectionRange> signal = textField.selectionSignal();

        textField.removeFromParent();
        UI.getCurrent().add(textField);

        fireSelectionChange(textField.getElement(), 0, 3, "Hel");
        Assertions.assertEquals(new SelectionRange(0, 3, "Hel"), signal.peek());

        // The cached signal must still be the same instance after re-attach.
        Assertions.assertSame(signal, textField.selectionSignal());
    }

    @Test
    void textArea_signalWorks() {
        TextArea area = new TextArea();
        UI.getCurrent().add(area);
        Signal<SelectionRange> signal = area.selectionSignal();

        fireSelectionChange(area.getElement(), 2, 7, "lorem");
        Assertions.assertEquals(new SelectionRange(2, 7, "lorem"),
                signal.peek());
    }

    private static void fireSelectionChange(Element element, int start, int end,
            String content) {
        ObjectNode data = JacksonUtils.createObjectNode();
        data.put("event.detail.start", start);
        data.put("event.detail.end", end);
        data.put("event.detail.content", content);
        DomEvent event = new DomEvent(element, "vaadin-selection-change", data);
        element.getNode().getFeature(ElementListenerMap.class).fireEvent(event);
    }
}
