package com.vaadin.flow.component.combobox.test.template;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.router.Route;

@Tag("combo-box-lit-page")
@JsModule("./src/combobox-lit-page.ts")
@Route("vaadin-combo-box/combo-box-lit-wrapper-page")
public class ComboBoxLitWrapperPage extends LitTemplate {

    @Id("cbw1")
    private ComboBoxLitWrapper comboBoxLitWrapper1;

    @Id("cbw2")
    private ComboBoxLitWrapper comboBoxLitWrapper2;

    @Id("cbw3")
    private ComboBoxLitWrapper comboBoxLitWrapper3;

    public ComboBoxLitWrapperPage() {
    }
}
