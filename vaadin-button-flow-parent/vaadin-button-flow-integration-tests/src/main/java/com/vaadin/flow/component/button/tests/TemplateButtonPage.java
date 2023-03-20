
package com.vaadin.flow.component.button.tests;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;

@Tag("template-button")
@Route("vaadin-button/template-button")
@JsModule("./template-button.js")
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
