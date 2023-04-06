
package com.vaadin.flow.component.radiobutton.tests;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.templatemodel.TemplateModel;

@JsModule("./src/detach-reattach.js")
@Tag("detach-reattach")
public class DetachReattachTemplate extends PolymerTemplate<TemplateModel> {
    @Id("testGroup")
    RadioButtonGroup<String> testGroup;

    public DetachReattachTemplate() {
        testGroup.setItems("A", "B", "C");
    }

    public void setRBGValue(String val) {
        testGroup.setValue(val);
    }

    public String getRBGValue() {
        return testGroup.getValue();
    }
}
