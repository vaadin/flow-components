package com.vaadin.flow.component.richtexteditor.examples;

import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route
public class MainView extends VerticalLayout {

    private Div valuePanel,
                htmlValuePanel,
                i18nPanel;

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
        setValueButton.addClickListener(event -> rte.setValue("[{\"insert\":\"Foo\"}]"));

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

        setHeight("100%");
        add(rte, setValueButton, getValueButton, getHtmlValueButton, setI18n, getI18n, valuePanel, htmlValuePanel, i18nPanel);
    }

    private RichTextEditor.RichTextEditorI18n createCustomI18n () {
        RichTextEditor.RichTextEditorI18n i18n = new RichTextEditor.RichTextEditorI18n()
                .setUndo("1").setRedo("2").setBold("3")
                .setItalic("4").setUnderline("5").setStrike("6")
                .setH1("7").setH2("8").setH3("9")
                .setSubscript("10").setSuperscript("11").setListOrdered("12")
                .setListBullet("13").setAlignLeft("14").setAlignCenter("15")
                .setAlignRight("16").setImage("17").setBlockquote("18")
                .setCodeBlock("19").setClean("20");
        return i18n;
    }
}
