/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.radiobutton.tests;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.template.Id;

@JsModule("./src/detach-reattach.js")
@Tag("detach-reattach")
public class DetachReattachTemplate extends LitTemplate {
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
