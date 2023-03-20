
package com.vaadin.flow.component.combobox.test.template;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.templatemodel.TemplateModel;

@Tag("wrapper-template")
@JsModule("./src/wrapper-template.js")
public class WrapperTemplate extends PolymerTemplate<TemplateModel> {

    @Id
    ComboBoxInATemplate comboBoxInATemplate;

    @Id
    ComboBoxInATemplate2 comboBoxInATemplate2;
}
