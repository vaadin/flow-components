/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.templatemodel.TemplateModel;

@Tag("demo-area")
@HtmlImport("frontend://src/demo-area.html")
public class DemoArea extends PolymerTemplate<TemplateModel> {

    private Component currentChart;

    @Id("background")
    private Div div;

    public void setContent(Component component) {
        component.getChildren().findFirst().get().getElement()
                .getClassList().add(component.getClass().getSimpleName());
        if (currentChart != null) {
            div.remove(currentChart);
        }

        div.add(component);
        currentChart = component;
    }
}
