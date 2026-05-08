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

import java.io.Serializable;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;

/**
 * Internal helper that lazily creates a {@link ValueSignal} of
 * {@link SelectionRange} for a {@link HasSelection} component and wires it to
 * the corresponding client-side selection events.
 * <p>
 * Cached on the component via
 * {@link ComponentUtil#setData(Component, Class, Object)} so subsequent calls
 * return the same signal instance.
 */
final class SelectionSignalSupport implements Serializable {

    private static final String INSTALL_JS = """
            const host = this;
            if (host._vaadinSelectionInstalled) return;
            host._vaadinSelectionInstalled = true;
            const fire = () => {
              const i = host.inputElement;
              if (!i) return;
              const start = i.selectionStart || 0;
              const end = i.selectionEnd || 0;
              const value = i.value || '';
              host.dispatchEvent(new CustomEvent('vaadin-selection-change', {
                detail: { start, end, content: value.substring(start, end) }
              }));
            };
            const ready = host.updateComplete || Promise.resolve();
            ready.then(() => {
              const i = host.inputElement;
              if (!i) return;
              ['select','keyup','mouseup','input','focus'].forEach(evt =>
                i.addEventListener(evt, fire));
              fire();
            });
            """;

    private final ValueSignal<SelectionRange> signal;

    private SelectionSignalSupport(Component component) {
        this.signal = new ValueSignal<>(SelectionRange.empty());

        Element element = component.getElement();

        element.addEventListener("vaadin-selection-change", e -> {
            int start = (int) e.getEventData().get("event.detail.start")
                    .asLong();
            int end = (int) e.getEventData().get("event.detail.end").asLong();
            String content = e.getEventData().get("event.detail.content")
                    .asString();
            signal.set(new SelectionRange(start, end, content));
        }).addEventData("event.detail.start").addEventData("event.detail.end")
                .addEventData("event.detail.content");

        element.addAttachListener(e -> element.executeJs(INSTALL_JS));
        if (component.isAttached()) {
            element.executeJs(INSTALL_JS);
        }
    }

    static Signal<SelectionRange> getOrCreate(Component component) {
        SelectionSignalSupport support = ComponentUtil.getData(component,
                SelectionSignalSupport.class);
        if (support == null) {
            support = new SelectionSignalSupport(component);
            ComponentUtil.setData(component, SelectionSignalSupport.class,
                    support);
        }
        return support.signal;
    }
}
