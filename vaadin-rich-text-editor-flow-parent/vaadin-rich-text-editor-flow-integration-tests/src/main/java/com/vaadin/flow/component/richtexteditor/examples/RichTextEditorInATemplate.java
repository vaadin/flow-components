package com.vaadin.flow.component.richtexteditor.examples;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.templatemodel.TemplateModel;

@Tag("rte-in-a-template")
@JsModule("rte-in-a-template.js")
public class RichTextEditorInATemplate extends PolymerTemplate<TemplateModel> {

    @Id
    RichTextEditor richTextEditor;

    public RichTextEditor getRichTextEditor() {
        return richTextEditor;
    }

}
