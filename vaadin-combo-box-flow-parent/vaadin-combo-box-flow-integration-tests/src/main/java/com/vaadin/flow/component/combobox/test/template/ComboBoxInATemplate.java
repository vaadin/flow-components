
package com.vaadin.flow.component.combobox.test.template;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.templatemodel.TemplateModel;

@Tag("combo-box-in-a-template")
@JsModule("./src/combo-box-in-a-template.js")
public class ComboBoxInATemplate extends PolymerTemplate<TemplateModel> {

    @Id
    ComboBox<String> comboBox;

    public ComboBox<String> getComboBox() {
        return comboBox;
    }

}
