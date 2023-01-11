package com.vaadin.flow.component.richtexteditor.tests;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.router.Route;

@Route("vaadin-rich-text-editor/value")
public class RichTextEditorValuePage extends Div {
    public RichTextEditorValuePage() {
        RichTextEditor editor = new RichTextEditor();

        NativeButton setValue = new NativeButton("Set value", e -> {
            editor.setValue("<h1>value</h1>");
        });
        setValue.setId("set-value");

        NativeButton setAsHtmlValue = new NativeButton("Set AsHtml value",
                e -> {
                    editor.asHtml().setValue("<h1>as-html-value</h1>");
                });
        setAsHtmlValue.setId("set-as-html-value");

        NativeButton setAsDeltaValue = new NativeButton("Set AsDelta value",
                e -> {
                    editor.asDelta().setValue(
                            "[{\"insert\":\"as-delta-value\"},{\"attributes\":{\"header\":1},\"insert\":\"\\n\"}]");
                });
        setAsDeltaValue.setId("set-as-delta-value");

        Span valueOutput = new Span();
        valueOutput.setId("value-output");
        editor.addValueChangeListener(
                e -> valueOutput.setText(formatEventData(e)));

        Span asHtmlValueOutput = new Span();
        asHtmlValueOutput.setId("as-html-value-output");
        editor.asHtml().addValueChangeListener(
                e -> asHtmlValueOutput.setText(formatEventData(e)));

        Span asDeltaValueOutput = new Span();
        asDeltaValueOutput.setId("as-delta-value-output");
        editor.asDelta().addValueChangeListener(
                e -> asDeltaValueOutput.setText(formatEventData(e)));

        NativeButton logValues = new NativeButton("Log values", e -> {
            valueOutput.setText(editor.getValue());
            asHtmlValueOutput.setText(editor.asHtml().getValue());
            asDeltaValueOutput.setText(editor.asDelta().getValue());
        });

        add(editor);
        add(new Div(setValue, setAsHtmlValue, setAsDeltaValue, logValues));
        add(new Div(new Span("Value Change Event: "), valueOutput));
        add(new Div(new Span("AsHtml Value Change Event: "),
                asHtmlValueOutput));
        add(new Div(new Span("AsDelta Value Change Event: "),
                asDeltaValueOutput));
    }

    private String formatEventData(
            HasValue.ValueChangeEvent<String> changeEvent) {
        return String.format("%s|%s|%s", changeEvent.getValue(),
                changeEvent.getOldValue(), changeEvent.isFromClient());
    }
}
