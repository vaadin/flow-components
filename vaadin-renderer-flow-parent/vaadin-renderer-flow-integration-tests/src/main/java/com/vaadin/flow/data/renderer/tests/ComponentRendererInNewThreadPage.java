/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
