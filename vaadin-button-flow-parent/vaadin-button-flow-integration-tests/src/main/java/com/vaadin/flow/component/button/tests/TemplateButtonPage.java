/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.button.tests;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;

@Tag("template-button")
@Route("vaadin-button/template-button")
@JsModule("./template-button.js")
@HtmlImport("template-button.html")
public class TemplateButtonPage extends PolymerTemplate<TemplateModel> {

    @Id("button")
    private Button templateButton;

    @Id("icon-button")
    private Button iconButton;

    public TemplateButtonPage() {
        setId("button-template");
        templateButton
                .addClickListener(event -> templateButton.setText("clicked"));
        iconButton.addClickListener(event -> iconButton.setText("clicked"));
    }
}
