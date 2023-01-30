package com.vaadin.flow.component.richtexteditor.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.router.Route;

@Route("vaadin-rich-text-editor/sanitization")
public class RichTextEditorSanitizationPage extends Div {
    public RichTextEditorSanitizationPage() {
        RichTextEditor editor = new RichTextEditor();

        NativeButton setUnsanitizedValue = new NativeButton(
                "Set unsanitized value", e -> {
                    // Img element requires a src in order to not be discarded
                    // by Quill. Server-side sanitization requires a base64
                    // encoded URL
                    String value = "<img onload=\"console.log('load')\" onerror=\"console.log('error')\" src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQIW2P4v5ThPwAG7wKklwQ/bwAAAABJRU5ErkJggg==\">"
                            + "<script>console.log('script')</script>";
                    editor.setValue(value);
                });
        setUnsanitizedValue.setId("set-unsanitized-value");

        Span valueOutput = new Span();
        editor.addValueChangeListener(e -> {
            valueOutput.setText(e.getValue());
        });
        valueOutput.setId("value-output");

        add(editor);
        add(new Div(new Span("Value: "), valueOutput));
        add(new Div(setUnsanitizedValue));
    }
}
