/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.data.renderer.tests;

import java.util.Arrays;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-renderer-flow/lit-renderer")
public class LitRendererPage extends Div {

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
                "Set simple LitRenderer", e -> component
                        .setRenderer(LitRenderer.of("<div>${index}</div>")));
        setSimpleLitRendererButton.setId("setSimpleLitRendererButton");
        add(setSimpleLitRendererButton);

        NativeButton setCheckboxRenderer = new NativeButton(
                "Set LitRenderer with checkbox", e -> {
                    component.setRenderer(LitRenderer.<String> of(
                            "<input type='checkbox' .checked='${live(item.checked)}'>")
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
        component.setRenderer(LitRenderer.<String> of(new StringBuilder()
                .append("<div tabindex=\"0\"")
                .append("  id=\"content-${index}\"")
                .append("  @click=\"${clicked}\"")
                .append("  draggable=\"true\" @dragstart=\"${dragged}\"")
                .append("  @keypress=\"${(e) => keyPressed(e.key)}\">")
                .append("  Item: ${item.value}").append("</div>").toString())
                .withProperty("value", ValueProvider.identity())
                .withFunction("clicked", item -> {
                    getElement()
                            .executeJs("console.warn(`event: clicked, item: "
                                    + item + "`)");
                }).withFunction("keyPressed", (item, args) -> {
                    getElement().executeJs(
                            "console.warn(`event: keyPressed, item: " + item
                                    + ", key: " + args.getString(0) + "`)");
                }).withFunction("dragged", (item, args) -> {
                    getElement()
                            .executeJs("console.warn(`event: dragged, item: "
                                    + item + ", argument count: "
                                    + args.length() + "`)");
                }));
    }

    private void setDetailsLitRenderer(
            LitRendererTestComponentWrapper component) {
        LitRenderer<String> renderer = LitRenderer.<String> of(
                "<div id=\"details-${index}\">Details: ${item.value}</div>");
        component.setDetailsRenderer(renderer);
        // Even though the properties are typically defined using the fluent
        // API, it should be possible to set them even after the renderer has
        // been set.
        renderer.withProperty("value", item -> item + " (details)");
    }

}
