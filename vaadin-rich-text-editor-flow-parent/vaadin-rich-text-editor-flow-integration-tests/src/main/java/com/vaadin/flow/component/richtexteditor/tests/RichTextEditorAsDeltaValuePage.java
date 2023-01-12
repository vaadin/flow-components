package com.vaadin.flow.component.richtexteditor.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import java.util.stream.Stream;

@Route(value = "vaadin-rich-text-editor/set-delta-value")
public class RichTextEditorAsDeltaValuePage extends Div {
    private int i = 0;

    public RichTextEditorAsDeltaValuePage() {
        final RichTextEditor rte = new RichTextEditor();
        final Div rteValue = new Div();
        rteValue.setId("rteValue");
        final Div rteDeltaValue = new Div();
        rteDeltaValue.setId("rteDeltaValue");
        final Div rteValueChangeMode = new Div();
        rteValueChangeMode.setId("rteValueChangeMode");
        final NativeButton button = new NativeButton("Set value", e -> rte
                .asDelta().setValue(String.format("[{\"insert\":\"Test %d\"},{\"attributes\":{\"header\":1},\"insert\":\"\\n\"}]", ++i)));
        button.setId("setValueButton");
        add(rte, rteValue, rteDeltaValue, rteValueChangeMode, button);
        Stream.of(ValueChangeMode.values())
                .map(v -> createValueChangeModeSetterButton(v, rte,
                        rteValueChangeMode))
                .forEach(this::add);
        rte.addValueChangeListener(e -> {
            rteValue.setText(rte.getValue());
            rteDeltaValue.setText(rte.asDelta().getValue());
        });
        rte.asDelta().setValue("[{\"insert\":\"Test\"},{\"attributes\":{\"header\":1},\"insert\":\"\\n\"}]");
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
