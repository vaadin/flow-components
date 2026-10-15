/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.router.Route;

@Route("vaadin-checkbox/injected-checkbox")
@Tag("inject-checkbox")
@JsModule("./inject-checkbox.js")
public class InjectedCheckboxPage extends LitTemplate {

    @Id("accept")
    private Checkbox checkbox;

    @Id("div")
    private Div div;

    public InjectedCheckboxPage() {
        checkbox.setValue(true);
    }
}
