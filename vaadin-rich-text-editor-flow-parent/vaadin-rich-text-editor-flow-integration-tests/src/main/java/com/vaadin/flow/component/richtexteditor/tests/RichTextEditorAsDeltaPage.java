package com.vaadin.flow.component.richtexteditor.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.Route;

import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Collectors;

@Route(value = "vaadin-rich-text-editor/as-delta")
public class RichTextEditorAsDeltaPage extends VerticalLayout {

    public RichTextEditorAsDeltaPage() {
        createSimpleRichTextEditor();
        createRichTextEditorWithBinder();
    }

    private void createSimpleRichTextEditor() {
        RichTextEditor rte = new RichTextEditor();
        rte.setId("simple-rte");
        rte.setValueChangeMode(ValueChangeMode.EAGER);

        Div simpleOutput = new Div();
        simpleOutput.setId("simple-output");

        rte.asDelta().addValueChangeListener(e -> {
            simpleOutput.setText(e.getValue());
        });

        Button setValueButton = new Button("Set server value");
        setValueButton.setId("set-server-value");
        setValueButton.addClickListener(
                event -> rte.asDelta().setValue("[{\"insert\":\"Foo\"}]"));

        add(rte, setValueButton, simpleOutput);
    }

    private void createRichTextEditorWithBinder() {
        RichTextEditor rte = new RichTextEditor();
        rte.setId("binder-rte");
        rte.setValueChangeMode(ValueChangeMode.EAGER);
        rte.setRequiredIndicatorVisible(true);

        Div binderOutput = new Div();
        binderOutput.setId("binder-output");

        // Configure Binder
        SerializablePredicate<String> notEmptyPredicate = value -> !rte
                .asDelta().getValue().trim().isEmpty();

        TestBean testBean = new TestBean();
        Binder<TestBean> binder = new Binder<>();
        binder.forField(rte.asDelta())
                .withValidator(notEmptyPredicate,
                        "Delta value should contain something")
                .bind(TestBean::getDeltaValue, TestBean::setDeltaValue);

        // Create action buttons
        Button save = new Button("Save");
        save.setId("binder-save");
        save.addClickListener(event -> {
            if (binder.writeBeanIfValid(testBean)) {
                binderOutput.setText("Saved: " + testBean.getDeltaValue());
            } else {
                BinderValidationStatus<TestBean> validate = binder.validate();
                String errorText = validate.getFieldValidationStatuses()
                        .stream().filter(BindingValidationStatus::isError)
                        .map(BindingValidationStatus::getMessage)
                        .map(Optional::get).distinct()
                        .collect(Collectors.joining(", "));
                binderOutput.setText("There are errors: " + errorText);
            }
        });

        Button reset = new Button("Reset");
        reset.setId("binder-reset");
        reset.addClickListener(event -> {
            // clear fields by setting null
            binder.readBean(null);
            binderOutput.setText("");
        });

        add(rte, new Div(save, reset), binderOutput);
    }

    private static class TestBean implements Serializable {
        private String deltaValue = "";

        public String getDeltaValue() {
            return deltaValue;
        }

        public void setDeltaValue(String deltaValue) {
            this.deltaValue = deltaValue;
        }
    }
}
