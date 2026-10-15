/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.data.renderer.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-renderer-flow/lit-renderer-property-namespace")
public class LitRendererPropertyNamespacePage extends Div {
    public LitRendererPropertyNamespacePage() {
        // Create a wrapper component with two LitRendererTestComponents
        LitRendererTestComponentWrapper wrapper = new LitRendererTestComponentWrapper(
                2);
        wrapper.setItems("Item 0");

        NativeButton setLitRenderersButton = new NativeButton(
                "Set LitRenderers", e -> {
                    for (int i = 0; i < wrapper.getComponentCount(); i++) {
                        wrapper.setRenderer(i, createLitRenderer(
                                "Component " + i + " : Default Renderer"));
                        wrapper.setDetailsRenderer(i, createLitRenderer(
                                "Component " + i + " : Details Renderer"));
                    }
                });
        setLitRenderersButton.setId("set-lit-renderers");

        NativeButton setComponentRenderersButton = new NativeButton(
                "Set ComponentRenderers", e -> {
                    for (int i = 0; i < wrapper.getComponentCount(); i++) {
                        wrapper.setRenderer(i, createComponentRenderer(
                                "Component " + i + " : Default Renderer"));
                        wrapper.setDetailsRenderer(i, createComponentRenderer(
                                "Component " + i + " : Details Renderer"));
                    }
                });
        setComponentRenderersButton.setId("set-component-renderers");

        NativeButton attachButton = new NativeButton("Attach", e -> {
            add(wrapper);
        });
        attachButton.setId("attach");

        add(setLitRenderersButton, setComponentRenderersButton, attachButton);
    }

    private LitRenderer<String> createLitRenderer(String prefix) {
        LitRenderer<String> renderer = LitRenderer
                .of("<span>${item.content}</span>");
        renderer.withProperty("content", item -> prefix + " : " + item);
        return renderer;
    }

    private ComponentRenderer<Span, String> createComponentRenderer(
            String prefix) {
        return new ComponentRenderer<>(item -> new Span(prefix + " : " + item));
    }
}
