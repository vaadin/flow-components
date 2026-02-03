/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.richtexteditor.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.router.Route;

@Route("vaadin-rich-text-editor/i18n")
public class RichTextEditorI18nPage extends Div {
    static final RichTextEditor.RichTextEditorI18n FULL_I18N = new RichTextEditor.RichTextEditorI18n()
            .setUndo("Undo custom").setRedo("Redo custom")
            .setBold("Bold custom").setItalic("Italic custom")
            .setUnderline("Underline custom").setStrike("Strike custom")
            .setColor("Color custom").setBackground("Background custom")
            .setH1("Header 1 custom").setH2("Header 2 custom")
            .setH3("Header 3 custom").setSubscript("Subscript custom")
            .setSuperscript("Superscript custom")
            .setListOrdered("Ordered list custom")
            .setListBullet("Bullet list custom").setOutdent("Outdent custom")
            .setIndent("Indent custom").setAlignLeft("Align left custom")
            .setAlignCenter("Align center custom")
            .setAlignRight("Align right custom").setImage("Image custom")
            .setLink("Link custom").setBlockquote("Blockquote custom")
            .setCodeBlock("Code block custom").setClean("Clean custom");

    public RichTextEditorI18nPage() {
        RichTextEditor editor = new RichTextEditor();

        NativeButton setI18n = new NativeButton("Set I18N",
                e -> editor.setI18n(FULL_I18N));
        setI18n.setId("set-i18n");

        NativeButton setEmptyI18n = new NativeButton("Set empty I18N",
                e -> editor.setI18n(new RichTextEditor.RichTextEditorI18n()));
        setEmptyI18n.setId("set-empty-i18n");

        add(editor, setI18n, setEmptyI18n);
    }
}
