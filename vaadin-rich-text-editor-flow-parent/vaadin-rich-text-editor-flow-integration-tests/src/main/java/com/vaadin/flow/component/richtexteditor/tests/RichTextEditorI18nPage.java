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
                .setH1("7").setH2("8").setH3("9").setSubscript("10")
                .setSuperscript("11").setListOrdered("12").setListBullet("13")
                .setAlignLeft("14").setAlignCenter("15").setAlignRight("16")
                .setImage("17").setLink("18").setBlockquote("19")
                .setCodeBlock("20").setClean("21");
    }
}
