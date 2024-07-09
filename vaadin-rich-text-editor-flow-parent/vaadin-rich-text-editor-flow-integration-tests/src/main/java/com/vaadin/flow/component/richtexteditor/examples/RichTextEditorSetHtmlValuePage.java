/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.richtexteditor.examples;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import java.util.stream.Stream;

@Route(value = "vaadin-rich-text-editor/set-html-value")
public class RichTextEditorSetHtmlValuePage extends Div {
    private int i = 0;

    public RichTextEditorSetHtmlValuePage() {
        final RichTextEditor rte = new RichTextEditor();
        final Div rteValue = new Div();
        rteValue.setId("rteValue");
        final Div rteHtmlValue = new Div();
        rteHtmlValue.setId("rteHtmlValue");
        final Div rteValueChangeMode = new Div();
        rteValueChangeMode.setId("rteValueChangeMode");
        final NativeButton button = new NativeButton("Set value", e -> rte
                .asHtml().setValue(String.format("<h1>Test %d</h1>", ++i)));
        button.setId("setValueButton");
        add(rte, rteValue, rteHtmlValue, rteValueChangeMode, button);
        Stream.of(ValueChangeMode.values())
                .map(v -> createValueChangeModeSetterButton(v, rte,
                        rteValueChangeMode))
                .forEach(this::add);
        rte.addValueChangeListener(e -> {
            rteValue.setText(rte.getValue());
            rteHtmlValue.setText(rte.getHtmlValue());
        });
        rte.asHtml().setValue("<h1>Test</h1>");
    }

    private static NativeButton createValueChangeModeSetterButton(
            ValueChangeMode valueChangeMode, RichTextEditor rte,
            Div rteValueChangeMode) {
        final String text = valueChangeMode.toString();
        final NativeButton button = new NativeButton(
                String.format("Set change mode to %s", text), e -> {
                    rte.setValueChangeMode(valueChangeMode);
                    rteValueChangeMode.setText(valueChangeMode.toString());
                });
        button.setId(String.format("setChangeMode_%s", text));
        return button;
    }
}
