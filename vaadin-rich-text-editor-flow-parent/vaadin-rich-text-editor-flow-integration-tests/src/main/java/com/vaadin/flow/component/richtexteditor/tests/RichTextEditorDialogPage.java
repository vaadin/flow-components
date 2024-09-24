/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.richtexteditor.tests;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.router.Route;

@Route("vaadin-rich-text-editor/dialog")
public class RichTextEditorDialogPage extends Div {
    public RichTextEditorDialogPage() {
        RichTextEditor editor = new RichTextEditor();
        editor.setValue("<ul><li>Item 1</li><li>Item 2</li></ul>");

        Dialog dialog = new Dialog();
        dialog.add(editor);
        dialog.open();
    }
}
