/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.richtexteditor.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.router.Route;

@Route("vaadin-rich-text-editor/detach-reattach")
public class RichTextEditorDetachReattachPage extends Div {
    public RichTextEditorDetachReattachPage() {
        RichTextEditor editor = new RichTextEditor();
        editor.setValue("<h1>foo</h1>");

        NativeButton detach = new NativeButton("Detach", e -> remove(editor));
        detach.setId("detach");

        NativeButton attach = new NativeButton("Attach", e -> add(editor));
        attach.setId("attach");

        add(editor);
        add(new Div(detach, attach));
    }
}
