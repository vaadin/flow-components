/**
 * Copyright 2000-2025 Vaadin Ltd.
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
    private final RichTextEditor editor;

    public RichTextEditorI18nPage() {
        editor = new RichTextEditor();
        add(editor);

        NativeButton setFullI18nButton = new NativeButton("set full i18n",
                e -> editor.setI18n(createFullI18n()));
        setFullI18nButton.setId("set-full-i18n");

        NativeButton setPartialI18nButton = new NativeButton("set partial i18n",
                e -> editor.setI18n(createPartialI18n()));
        setPartialI18nButton.setId("set-partial-i18n");

        NativeButton detachButton = new NativeButton("detach",
                e -> remove(editor));
        detachButton.setId("detach");

        NativeButton attachButton = new NativeButton("attach",
                e -> add(editor));
        attachButton.setId("attach");

        add(setFullI18nButton, setPartialI18nButton, detachButton,
                attachButton);
    }

    private RichTextEditor.RichTextEditorI18n createFullI18n() {
        //@formatter:off
        return new RichTextEditor.RichTextEditorI18n()
                .setUndo("Undo custom")
                .setRedo("Redo custom")
                .setBold("Bold custom")
                .setItalic("Italic custom")
                .setUnderline("Underline custom")
                .setStrike("Strike custom")
                .setColor("Color custom")
                .setBackground("Background custom")
                .setH1("Header 1 custom")
                .setH2("Header 2 custom")
                .setH3("Header 3 custom")
                .setSubscript("Subscript custom")
                .setSuperscript("Superscript custom")
                .setListOrdered("Ordered list custom")
                .setListBullet("Bullet list custom")
                .setAlignLeft("Align left custom")
                .setAlignCenter("Align center custom")
                .setAlignRight("Align right custom")
                .setImage("Image custom")
                .setLink("Link custom")
                .setBlockquote("Blockquote custom")
                .setCodeBlock("Code block custom")
                .setClean("Clean custom");
        //@formatter:on
    }

    private RichTextEditor.RichTextEditorI18n createPartialI18n() {
        //@formatter:off
        return new RichTextEditor.RichTextEditorI18n()
                .setUndo("Undo custom")
                .setRedo("Redo custom");
        //@formatter:on
    }
}
