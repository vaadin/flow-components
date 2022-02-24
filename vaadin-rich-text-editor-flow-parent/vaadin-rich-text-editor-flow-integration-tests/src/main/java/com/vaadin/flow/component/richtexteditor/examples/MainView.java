package com.vaadin.flow.component.richtexteditor.examples;

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

    private Div valuePanel, htmlValuePanel, i18nPanel;

    public MainView() {
        valuePanel = new Div();
        valuePanel.setId("valuePanel");

        htmlValuePanel = new Div();
        htmlValuePanel.setId("htmlValuePanel");

        i18nPanel = new Div();
        i18nPanel.setId("i18nPanel");

        RichTextEditor rte = new RichTextEditor();

        Button setValueButton = new Button("Set value");
        setValueButton.setId("setValue");
        setValueButton.addClickListener(
                event -> rte.setValue("[{\"insert\":\"Foo\"}]"));

        Button getValueButton = new Button("Get value");
        getValueButton.setId("getValue");
        getValueButton.addClickListener(event -> {
            String value = rte.getValue();
            valuePanel.setText(value);
        });

        Button getHtmlValueButton = new Button("Get htmlValue");
        getHtmlValueButton.setId("getHtmlValue");
        getHtmlValueButton.addClickListener(event -> {
            String htmlValue = rte.getHtmlValue();
            htmlValuePanel.setText(htmlValue);
        });

        Button setI18n = new Button("Set Custom i18n");
        setI18n.setId("setI18n");
        setI18n.addClickListener(event -> {
            RichTextEditor.RichTextEditorI18n i18n = createCustomI18n();
            rte.setI18n(i18n);
        });

        Button getI18n = new Button("Get i18n");
        getI18n.setId("getI18n");
        getI18n.addClickListener(event -> {
            if (rte.getI18n() != null) {
                i18nPanel.setText(rte.getI18n().toString());
            } else {
                i18nPanel.setText("null");
            }
        });

        add(rte, setValueButton, getValueButton, getHtmlValueButton, setI18n,
                getI18n, valuePanel, htmlValuePanel, i18nPanel);

        createRichTextEditorWithBinder();

        createRichTextEditorInATemplate();

        createRichTextEditorWithHtmlBinder();
    }

    private RichTextEditor.RichTextEditorI18n createCustomI18n() {
        RichTextEditor.RichTextEditorI18n i18n = new RichTextEditor.RichTextEditorI18n()
                .setUndo("1").setRedo("2").setBold("3").setItalic("4")
                .setUnderline("5").setStrike("6").setH1("7").setH2("8")
                .setH3("9").setSubscript("10").setSuperscript("11")
                .setListOrdered("12").setListBullet("13").setAlignLeft("14")
                .setAlignCenter("15").setAlignRight("16").setImage("17")
                .setLink("18").setBlockquote("19").setCodeBlock("20")
                .setClean("21");
        return i18n;
    }

    private void createRichTextEditorInATemplate() {
        RichTextEditorInATemplate richTextEditorInATemplate = new RichTextEditorInATemplate();
        richTextEditorInATemplate.setId("template");
        RichTextEditor rteTemplate = richTextEditorInATemplate
                .getRichTextEditor();

        Div valuePanel = new Div();
        valuePanel.setId("template-value-panel");

        Button getValueButton = new Button("Get value");
        getValueButton.setId("get-template-rte-value");
        getValueButton.addClickListener(event -> {
            String value = rteTemplate.getValue();
            valuePanel.setText(value);
        });

        add(richTextEditorInATemplate, valuePanel, getValueButton);
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
