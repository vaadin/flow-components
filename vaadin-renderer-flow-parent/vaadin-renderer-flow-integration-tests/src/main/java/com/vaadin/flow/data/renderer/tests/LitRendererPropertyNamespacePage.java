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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-renderer-flow/lit-renderer-property-namespace")
public class LitRendererPropertyNamespacePage extends Div {
    public LitRendererPropertyNamespacePage() {
        LitRendererTestComponentWrapper wrapper = new LitRendererTestComponentWrapper(
                2);
        wrapper.setItems("0");

        NativeButton setLitRenderersButton = new NativeButton(
                "Set LitRenderers", e -> {
                    wrapper.setRenderer(createDefaultLitRenderer());
                    wrapper.setDetailsRenderer(createDetailsLitRenderer());
                });
        setLitRenderersButton.setId("set-lit-renderers");

        NativeButton setComponentRenderersButton = new NativeButton(
                "Set ComponentRenderers", e -> {
                    wrapper.setRenderer(createDefaultComponentRenderer());
                    wrapper.setDetailsRenderer(
                            createDetailsComponentRenderer());
                });
        setComponentRenderersButton.setId("set-component-renderers");

        add(wrapper, setLitRenderersButton, setComponentRenderersButton);
    }

    private LitRenderer<String> createDefaultLitRenderer() {
        LitRenderer<String> renderer = LitRenderer
                .of("<span>${item.content}</span>");
        renderer.withProperty("content", item -> "Default renderer: " + item);
        return renderer;
    }

    private LitRenderer<String> createDetailsLitRenderer() {
        LitRenderer<String> renderer = LitRenderer
                .of("<span>${item.content}</span>");
        renderer.withProperty("content", item -> "Details renderer: " + item);
        return renderer;
    }

    private ComponentRenderer<Span, String> createDefaultComponentRenderer() {
        return new ComponentRenderer<>(
                item -> new Span("Default renderer: " + item));
    }

    private ComponentRenderer<Span, String> createDetailsComponentRenderer() {
        return new ComponentRenderer<>(
                item -> new Span("Details renderer: " + item));
    }
}
