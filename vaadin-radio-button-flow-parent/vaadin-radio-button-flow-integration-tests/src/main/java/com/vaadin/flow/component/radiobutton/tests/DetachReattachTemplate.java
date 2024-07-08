/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.radiobutton.tests;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.templatemodel.TemplateModel;

@JsModule("./src/detach-reattach.js")
@HtmlImport("src/detach-reattach.html")
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
