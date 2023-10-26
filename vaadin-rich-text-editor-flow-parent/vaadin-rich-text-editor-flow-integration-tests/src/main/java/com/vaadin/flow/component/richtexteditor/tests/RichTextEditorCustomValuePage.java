package com.vaadin.flow.component.richtexteditor.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.router.Route;

@Route("vaadin-rich-text-editor/custom-value")
public class RichTextEditorCustomValuePage extends Div {
    private RichTextEditor editor;
    private Button setAsDeltaValue;

    public RichTextEditorCustomValuePage() {
        editor = new RichTextEditor();

        Input customValueInput = new Input();
        customValueInput.setId("custom-value-input");

        setAsDeltaValue = new Button("Set as delta value", e -> {
            editor.asDelta().setValue(customValueInput.getValue());
        });
        setAsDeltaValue.setDisableOnClick(true);
        setAsDeltaValue.setId("set-as-delta-value");

        add(editor);
        add(new Div(customValueInput));
        add(new Div(setAsDeltaValue));
    }
}
