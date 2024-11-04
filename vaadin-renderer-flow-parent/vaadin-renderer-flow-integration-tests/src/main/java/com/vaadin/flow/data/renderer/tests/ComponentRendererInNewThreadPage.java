/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.data.renderer.tests;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.communication.PushMode;

/**
 * Page created for testing purposes. Not suitable for demos.
 *
 * @author Vaadin Ltd.
 *
 */
@Route("vaadin-renderer-flow/component-renderer-in-new-thread")
public class ComponentRendererInNewThreadPage extends Div {
    public ComponentRendererInNewThreadPage() {
        UI.getCurrent().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

        LitRendererTestComponentWrapper component = new LitRendererTestComponentWrapper();
        component.setItems("Item");

        ComponentRenderer<Span, String> componentRenderer = new ComponentRenderer<Span, String>(
                item -> new Span(item));

        NativeButton addComponentRendererBeforeAttach = new NativeButton(
                "Add component renderer before attach", (event) -> {
                    runInBackgroundThread(() -> {
                        component.setRenderer(componentRenderer);
                        add(component);
                    });
                });
        addComponentRendererBeforeAttach
                .setId("add-component-renderer-before-attach");

        NativeButton addComponentRendererAfterAttach = new NativeButton(
                "Add component renderer after attach", (event) -> {
                    runInBackgroundThread(() -> {
                        add(component);
                        component.setRenderer(componentRenderer);
                    });
                });
        addComponentRendererAfterAttach
                .setId("add-component-renderer-after-attach");

        add(addComponentRendererBeforeAttach, addComponentRendererAfterAttach);
    }

    private void runInBackgroundThread(Command command) {
        VaadinSession session = VaadinSession.getCurrent();
        new Thread(() -> session.accessSynchronously(command)).start();
    }
}
