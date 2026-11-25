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
package com.vaadin.flow.data.renderer.tests;

import java.util.Arrays;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.renderer.CompiledLitTemplate;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.js.JsInvokers;
import com.vaadin.flow.js.JsSnippets;
import com.vaadin.flow.router.Route;

@Route("vaadin-renderer-flow/lit-renderer")
public class LitRendererPage extends Div {

    @JsDefinition
    interface ConsoleJs extends JsInvokers {
        @JsExpression("console.warn($0)")
        void warn(String message);
    }

    @JsDefinition
    interface Templates extends JsSnippets {
        @JsExpression("""
                return html`<div tabindex="0"
                    id="content-${index}"
                    @click=${functions.clicked}
                    draggable="true" @dragstart=${functions.dragged}
                    @keypress=${(e) => functions.keyPressed(e.key)}>Item: ${item.value}</div>`;
                """)
        CompiledLitTemplate content();

        @JsExpression("return html`<div>${index}</div>`;")
        CompiledLitTemplate index();

        @JsExpression("return html`<input type='checkbox' .checked=${live(item.checked)}>`;")
        CompiledLitTemplate checkbox();

        @JsExpression("return html`<div id=\"details-${index}\">Details: ${item.value}</div>`;")
        CompiledLitTemplate details();
    }

    private static final Templates TEMPLATES = JsSnippets.of(Templates.class);

    public LitRendererPage() {

        LitRendererTestComponentWrapper component = new LitRendererTestComponentWrapper();
        component.setItems(Arrays.asList("0", "1", "2", "3", "4"));
        setLitRenderer(component);

        Div componentWrapper = new Div();
        componentWrapper.add(component);
        add(componentWrapper);

        add(new Div(new Text("Main content:")));

        NativeButton setLitRendererButton = new NativeButton("Set LitRenderer",
                e -> setLitRenderer(component));
        setLitRendererButton.setId("setLitRendererButton");
        add(setLitRendererButton);

        NativeButton setSimpleLitRendererButton = new NativeButton(
                "Set simple LitRenderer",
                e -> component.setRenderer(LitRenderer.of(TEMPLATES.index())));
        setSimpleLitRendererButton.setId("setSimpleLitRendererButton");
        add(setSimpleLitRendererButton);

        NativeButton setCheckboxRenderer = new NativeButton(
                "Set LitRenderer with checkbox", e -> {
                    component.setRenderer(
                            LitRenderer.<String> of(TEMPLATES.checkbox())
                                    .withProperty("checked", "2"::equals));
                });
        setCheckboxRenderer.setId("setCheckboxRenderer");
        add(setCheckboxRenderer);

        NativeButton removeRendererButton = new NativeButton("Remove renderer",
                e -> component.setRenderer(null));
        removeRendererButton.setId("removeRendererButton");
        add(removeRendererButton);

        NativeButton longRefreshButton = new NativeButton("Long data refresh",
                e -> {
                    try {
                        // Simulate heavy work
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) { // NOSONAR
                    }
                    component.setItems(Arrays.asList("0", "1", "2", "3", "4"));
                });
        longRefreshButton.setId("longRefreshButton");
        add(longRefreshButton);

        add(new Div(new Text("Details:")));

        NativeButton setDetailsLitRendererButton = new NativeButton(
                "Set details LitRenderer",
                e -> setDetailsLitRenderer(component));
        setDetailsLitRendererButton.setId("setDetailsLitRendererButton");
        add(setDetailsLitRendererButton);

        add(new Div(new Text("Component:")));
        NativeButton toggleAttachedButton = new NativeButton("Toggle attached",
                e -> {
                    if (component.isAttached()) {
                        componentWrapper.remove(component);
                    } else {
                        componentWrapper.add(component);
                    }
                });
        toggleAttachedButton.setId("toggleAttachedButton");
        add(toggleAttachedButton);
    }

    private void setLitRenderer(LitRendererTestComponentWrapper component) {
        component.setRenderer(LitRenderer.<String> of(TEMPLATES.content())
                .withProperty("value", ValueProvider.identity())
                .withFunction("clicked", item -> {
                    warn("event: clicked, item: " + item);
                }).withFunction("keyPressed", (item, args) -> {
                    warn("event: keyPressed, item: " + item + ", key: "
                            + args.get(0).asString());
                }).withFunction("dragged", (item, args) -> {
                    warn("event: dragged, item: " + item + ", argument count: "
                            + args.size());
                }));
    }

    private void warn(String message) {
        getElement().executeJs(ConsoleJs.class).warn(message);
    }

    private void setDetailsLitRenderer(
            LitRendererTestComponentWrapper component) {
        LitRenderer<String> renderer = LitRenderer
                .<String> of(TEMPLATES.details());
        component.setDetailsRenderer(renderer);
        // Even though the properties are typically defined using the fluent
        // API, it should be possible to set them even after the renderer has
        // been set.
        renderer.withProperty("value", item -> item + " (details)");
    }

}
