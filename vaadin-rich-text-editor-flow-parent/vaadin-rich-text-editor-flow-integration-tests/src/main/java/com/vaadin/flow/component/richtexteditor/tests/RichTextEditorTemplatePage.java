/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.richtexteditor.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.router.Route;

@Route("vaadin-rich-text-editor/template")
public class RichTextEditorTemplatePage extends Div {
    public RichTextEditorTemplatePage() {
        RichTextEditorInATemplate template = new RichTextEditorInATemplate();
        RichTextEditor editor = template.getRichTextEditor();

        Div valueOutput = new Div();
        valueOutput.setId("value-output");

        NativeButton getValueButton = new NativeButton("Get value", event -> {
            String value = editor.getValue();
            valueOutput.setText(value);
        });
        getValueButton.setId("get-value");

        add(template, valueOutput, getValueButton);
    }
}
