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
package com.vaadin.flow.component.shared.internal;

import java.io.Serializable;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.shared.HasTextSelection;
import com.vaadin.flow.component.shared.HasTextSelection.TextSelectionJs;
import com.vaadin.flow.component.shared.SelectionRange;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;

import tools.jackson.databind.JsonNode;

/**
 * Keeps the selection signal of a {@link HasTextSelection} component in sync
 * with the selection of its input element.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
public final class SelectionSignalSupport implements Serializable {

    private static final String EVENT_NAME = "vaadin-text-selection-change";
    private static final String START = "event.detail.start";
    private static final String END = "event.detail.end";
    private static final String CONTENT = "event.detail.content";

    private final ValueSignal<SelectionRange> signal = new ValueSignal<>(
            SelectionRange.empty());
    private final Signal<SelectionRange> readonlySignal = signal.asReadonly();

    private SelectionSignalSupport(Component component) {
        Element element = component.getElement();
        element.addEventListener(EVENT_NAME, this::onSelectionChange)
                .addEventData(START).addEventData(END).addEventData(CONTENT);
        element.addAttachListener(e -> installSelectionListener(element));
        if (component.isAttached()) {
            installSelectionListener(element);
        }
    }

    /**
     * Returns the selection signal of the given component, creating it and
     * wiring it to the client on the first call.
     *
     * @param component
     *            the component to get the selection signal for
     * @return the read-only selection signal, never {@code null}
     */
    public static Signal<SelectionRange> getOrCreate(Component component) {
        SelectionSignalSupport support = ComponentUtil.getData(component,
                SelectionSignalSupport.class);
        if (support == null) {
            support = new SelectionSignalSupport(component);
            ComponentUtil.setData(component, SelectionSignalSupport.class,
                    support);
        }
        return support.readonlySignal;
    }

    private static void installSelectionListener(Element element) {
        element.executeJs(TextSelectionJs.class).installSelectionListener();
    }

    private void onSelectionChange(DomEvent event) {
        JsonNode data = event.getEventData();
        if (!data.path(START).isNumber() || !data.path(END).isNumber()
                || !data.path(CONTENT).isString()) {
            return;
        }
        int start = data.get(START).asInt();
        int end = data.get(END).asInt();
        String content = data.get(CONTENT).asString();
        // Ignore tampered values instead of failing in the record constructor
        if (start < 0 || end < start) {
            return;
        }
        signal.set(new SelectionRange(start, end, content));
    }
}
