package com.vaadin.flow.component.richtexteditor.tests;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.component.template.Id;

@Tag("rte-in-a-template")
@JsModule("rte-in-a-template.js")
public class RichTextEditorInATemplate extends LitTemplate {

    @Id
    RichTextEditor richTextEditor;

    public RichTextEditor getRichTextEditor() {
        return richTextEditor;
    }

}
