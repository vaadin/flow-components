/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.richtexteditor.examples;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.templatemodel.TemplateModel;

@Tag("rte-in-a-template")
@HtmlImport("src/rte-in-a-template.html")
@JsModule("rte-in-a-template.js")
public class RichTextEditorInATemplate extends PolymerTemplate<TemplateModel> {

    @Id
    RichTextEditor richTextEditor;

    public RichTextEditor getRichTextEditor() {
        return richTextEditor;
    }

}
