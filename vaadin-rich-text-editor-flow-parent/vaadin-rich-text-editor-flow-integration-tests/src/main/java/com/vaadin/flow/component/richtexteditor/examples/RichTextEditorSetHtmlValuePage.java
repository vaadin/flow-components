package com.vaadin.flow.component.richtexteditor.examples;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-rich-text-editor/set-html-value")
public class RichTextEditorSetHtmlValuePage extends Div {
    private int i = 0;

    public RichTextEditorSetHtmlValuePage() {
        final RichTextEditor rte = new RichTextEditor();
        final Div rteValue = new Div();
        rteValue.setId("rteValue");
        final Div rteHtmlValue = new Div();
        rteHtmlValue.setId("rteHtmlValue");
        final NativeButton button = new NativeButton("Set value", e -> rte.asHtml()
            .setValue(String.format("<h1>Test %d</h1>", ++i)));
        button.setId("setValueButton");
        add(rte, rteValue, rteHtmlValue, button);
        rte.addValueChangeListener(e -> {
            rteValue.setText(rte.getValue());
            rteHtmlValue.setText(rte.getHtmlValue());
        });
        rte.asHtml().setValue("<h1>Test</h1>");
    }

}
