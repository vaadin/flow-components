package com.vaadin.flow.component.combobox.test.template;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.data.provider.ListDataProvider;

import java.util.Arrays;

@Tag("combo-box-lit-template-wrapper")
@JsModule("./src/combo-box-lit-template-wrapper.ts")
public class ComboBoxLitTemplateWrapper extends LitTemplate {

    @Id("cb")
    private ComboBox<String> comboBox;

    public ComboBoxLitTemplateWrapper() {
        comboBox.setDataProvider(
                new ListDataProvider<>(Arrays.asList("B", "C", "A", "D")));
        comboBox.setValue("D");
    }
}
