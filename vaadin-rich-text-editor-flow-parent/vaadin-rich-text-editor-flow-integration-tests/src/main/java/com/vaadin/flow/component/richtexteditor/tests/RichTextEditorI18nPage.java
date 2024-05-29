package com.vaadin.flow.component.richtexteditor.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.router.Route;

@Route("vaadin-rich-text-editor/i18n")
public class RichTextEditorI18nPage extends Div {
    public RichTextEditorI18nPage() {
        RichTextEditor editor = new RichTextEditor();
        editor.setI18n(createCustomI18n());

        Div i18nOutput = new Div();
        i18nOutput.setId("i18n-output");
        i18nOutput.setText(editor.getI18n().toString());

        add(editor);
        add(i18nOutput);
    }

    private RichTextEditor.RichTextEditorI18n createCustomI18n() {
        return new RichTextEditor.RichTextEditorI18n().setUndo("1").setRedo("2")
                .setBold("3").setItalic("4").setUnderline("5").setStrike("6")
                .setColor("7").setBackground("8").setH1("9").setH2("10")
                .setH3("11").setSubscript("12").setSuperscript("13")
                .setListOrdered("14").setListBullet("15").setAlignLeft("16")
                .setAlignCenter("17").setAlignRight("18").setImage("19")
                .setLink("20").setBlockquote("21").setCodeBlock("22")
                .setClean("23");
    }
}
