package com.vaadin.flow.component.combobox.test.template;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.router.Route;

@Tag("combo-box-lit-wrapper-lit-template-page")
@JsModule("./src/combo-box-lit-wrapper-lit-template-page.ts")
@Route("vaadin-combo-box/combo-box-lit-wrapper-lit-template-page")
public class ComboBoxLitWrapperLitTemplatePage extends LitTemplate {

    @Id("cbw1")
    private ComboBoxLitTemplateWrapper comboBoxLitWrapper1;

    @Id("cbw2")
    private ComboBoxLitTemplateWrapper comboBoxLitWrapper2;

    @Id("cbw3")
    private ComboBoxLitTemplateWrapper comboBoxLitWrapper3;

    public ComboBoxLitWrapperLitTemplatePage() {
    }
}
