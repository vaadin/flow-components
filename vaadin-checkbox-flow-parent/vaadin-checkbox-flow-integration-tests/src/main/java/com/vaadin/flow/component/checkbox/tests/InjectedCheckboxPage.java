
package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;

@Route("vaadin-checkbox/injected-checkbox")
@Tag("inject-checkbox")
@JsModule("./inject-checkbox.js")
public class InjectedCheckboxPage extends PolymerTemplate<TemplateModel>
        implements HasComponents {

    @Id("accept")
    private Checkbox checkbox;

    @Id("div")
    private Div div;

    public InjectedCheckboxPage() {
        checkbox.setValue(true);
    }
}
