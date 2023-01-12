package com.vaadin.flow.component.richtexteditor.tests;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.Binding;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.Route;

import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Collectors;

@Route(value = "vaadin-rich-text-editor")
public class MainView extends VerticalLayout {
    public MainView() {
        createRichTextEditorWithBinder();

        createRichTextEditorWithHtmlBinder();
    }

    private void createRichTextEditorWithBinder() {
        RichTextEditor rteWithBinder = new RichTextEditor();

        Div valuePanel = new Div();
        valuePanel.setId("binder-value-panel");

        Div infoPanel = new Div();
        Binder<Entry> binder = new Binder<>();

        // The object that will be edited
        Entry entryBeingEdited = new Entry();

        rteWithBinder.setValueChangeMode(ValueChangeMode.EAGER);

        // Create the action buttons
        Button save = new Button("Save");
        Button reset = new Button("Reset");
        Button getValueButton = new Button("Get value");
        getValueButton.setId("get-binder-rte-value");
        getValueButton.addClickListener(event -> {
            String value = rteWithBinder.getValue();
            valuePanel.setText(value);
        });

        // Button bar
        HorizontalLayout actions = new HorizontalLayout();
        actions.add(save, reset, getValueButton);
        save.getStyle().set("marginRight", "10px");

        SerializablePredicate<String> deltaValuePredicate = value -> !rteWithBinder
                .getValue().trim().isEmpty();

        Binding<Entry, String> deltaValueBinding = binder
                .forField(rteWithBinder)
                .withValidator(deltaValuePredicate,
                        "Delta value should contain something")
                .bind(Entry::getDeltaValue, Entry::setDeltaValue);

        // Editor is a required field
        rteWithBinder.setRequiredIndicatorVisible(true);

        // Click listeners for the buttons
        save.addClickListener(event -> {
            if (binder.writeBeanIfValid(entryBeingEdited)) {
                infoPanel.setText("Saved bean values: " + entryBeingEdited);
            } else {
                BinderValidationStatus<Entry> validate = binder.validate();
                String errorText = validate.getFieldValidationStatuses()
                        .stream().filter(BindingValidationStatus::isError)
                        .map(BindingValidationStatus::getMessage)
                        .map(Optional::get).distinct()
                        .collect(Collectors.joining(", "));
                infoPanel.setText("There are errors: " + errorText);
            }
        });
        reset.addClickListener(event -> {
            // clear fields by setting null
            binder.readBean(null);
            infoPanel.setText("");
        });

        infoPanel.setId("binder-info");
        rteWithBinder.setId("binder-delta-value");
        save.setId("binder-save");
        reset.setId("binder-reset");

        add(rteWithBinder, actions, infoPanel, valuePanel);
    }

    private void createRichTextEditorWithHtmlBinder() {
        RichTextEditor rte = new RichTextEditor();
        rte.setId("html-rte");
        add(rte);

        Div valuePanel = new Div();
        valuePanel.setId("html-binder-value-panel");

        Div infoPanel = new Div();
        Binder<HtmlEntry> binder = new Binder<>();

        // The object that will be edited
        HtmlEntry entryBeingEdited = new HtmlEntry();

        // Create the action buttons
        Button save = new Button("Save");
        Button reset = new Button("Reset");
        Button setBeanHtmlValue = new Button("Set bean html value");
        Button getValueButton = new Button("Get value");
        getValueButton.setId("get-html-binder-rte-value");
        getValueButton.addClickListener(event -> {
            String value = rte.asHtml().getValue();
            String webcomponentValue = rte.getElement()
                    .getProperty("htmlValue");
            valuePanel.setText(value + ' ' + webcomponentValue);
        });

        // Button bar
        HorizontalLayout actions = new HorizontalLayout();
        actions.add(save, reset, getValueButton, setBeanHtmlValue);
        save.getStyle().set("marginRight", "10px");

        SerializablePredicate<String> htmlValuePredicate = value -> {
            String htmlValue = rte.asHtml().getValue();
            return htmlValue != null && !htmlValue.trim().isEmpty();
        };

        Binding<HtmlEntry, String> asHtmlValueBinding = binder
                .forField(rte.asHtml())
                .withValidator(htmlValuePredicate,
                        "html value should contain something")
                .bind(HtmlEntry::getHtmlValue, HtmlEntry::setHtmlValue);

        // Editor is a required field
        rte.asHtml().setRequiredIndicatorVisible(true);

        // Click listeners for the buttons
        save.addClickListener(event -> {
            if (binder.writeBeanIfValid(entryBeingEdited)) {
                infoPanel.setText("Saved bean values: " + entryBeingEdited);
            } else {
                BinderValidationStatus<HtmlEntry> validate = binder.validate();
                String errorText = validate.getFieldValidationStatuses()
                        .stream().filter(BindingValidationStatus::isError)
                        .map(BindingValidationStatus::getMessage)
                        .map(Optional::get).distinct()
                        .collect(Collectors.joining(", "));
                infoPanel.setText("There are errors: " + errorText);
            }
        });
        reset.addClickListener(event -> {
            // clear fields by setting null
            binder.readBean(null);
            infoPanel.setText("");
        });
        setBeanHtmlValue.addClickListener(event -> {
            entryBeingEdited.setHtmlValue("<p><b>Foo</b></p>");
            binder.readBean(entryBeingEdited);
        });

        infoPanel.setId("html-binder-info");
        save.setId("html-binder-save");
        setBeanHtmlValue.setId("html-binder-set-bean-value");
        reset.setId("html-binder-reset");

        add(actions, infoPanel, valuePanel);
    }

    /**
     * Example Bean for the Form with Binder.
     */
    private static class Entry implements Serializable {

        private String deltaValue = "";

        public String getDeltaValue() {
            return deltaValue;
        }

        public void setDeltaValue(String deltaValue) {
            this.deltaValue = deltaValue;
        }

        @Override
        public String toString() {
            return "Contact{" + "deltaValue='" + deltaValue + '\'' + '}';
        }
    }

    /**
     * Example Bean for the Form with Html Binder.
     */
    private static class HtmlEntry implements Serializable {

        private String htmlValue = "";

        public String getHtmlValue() {
            return htmlValue;
        }

        public void setHtmlValue(String htmlValue) {
            this.htmlValue = htmlValue;
        }

        @Override
        public String toString() {
            return "Contact{" + "htmlValue='" + htmlValue + '\'' + '}';
        }
    }
}
