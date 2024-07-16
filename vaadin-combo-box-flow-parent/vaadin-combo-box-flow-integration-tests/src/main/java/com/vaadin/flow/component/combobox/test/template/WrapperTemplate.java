/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test.template;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.templatemodel.TemplateModel;

@Tag("wrapper-template")
@HtmlImport("src/wrapper-template.html")
@JsModule("./src/wrapper-template.js")
public class WrapperTemplate extends PolymerTemplate<TemplateModel> {

    @Id
    ComboBoxInATemplate comboBoxInATemplate;

    @Id
    ComboBoxInATemplate2 comboBoxInATemplate2;
}
