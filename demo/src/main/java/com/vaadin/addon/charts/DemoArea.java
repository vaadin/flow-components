package com.vaadin.addon.charts;

import com.vaadin.flow.model.TemplateModel;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tag;
import com.vaadin.ui.common.HtmlImport;
import com.vaadin.ui.html.Div;
import com.vaadin.ui.polymertemplate.Id;
import com.vaadin.ui.polymertemplate.PolymerTemplate;

@Tag("demo-area")
@HtmlImport("frontend://src/demo-area.html")
public class DemoArea extends PolymerTemplate<TemplateModel> {

    private Component currentChart;

    @Id("background")
    private Div div;

    public void setContent(Component component) {
        if (currentChart != null) {
            div.remove(currentChart);
        }

        div.add(component);
        currentChart = component;
    }
}
