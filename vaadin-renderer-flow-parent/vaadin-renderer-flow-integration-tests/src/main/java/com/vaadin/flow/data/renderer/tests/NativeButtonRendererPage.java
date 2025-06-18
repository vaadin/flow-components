/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.data.renderer.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-renderer-flow/native-button-renderer")
public class NativeButtonRendererPage extends Div {

    public NativeButtonRendererPage() {
        ValueProvider<String, String> labelProvider = (item) -> "Label " + item;
        NativeButtonRenderer<String> renderer = new NativeButtonRenderer<>(
                labelProvider);

        var component = renderer.createComponent("Item");
        component.setId("nativeBtn");

        add(component);
    }
}
