
package com.vaadin.flow.component.notification.tests;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.templatemodel.TemplateModel;

/**
 * Simple template example.
 */
@Tag("vaadin-notification-flow-test-template")
@JsModule("vaadin-notification-flow-test-template.js")
public class TestTemplate extends PolymerTemplate<TemplateModel> {

    @Id("container")
    private Div div;

    @Id("btn")
    private NativeButton button;

    private int count;

    public TestTemplate() {
        button.addClickListener(event -> {
            count++;
            int id = count;
            Label label = new Label("Label " + id);
            label.setId("label-" + id);
            div.add(label);
        });
    }
}
