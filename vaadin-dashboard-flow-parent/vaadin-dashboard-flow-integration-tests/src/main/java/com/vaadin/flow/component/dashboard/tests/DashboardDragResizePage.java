/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard.tests;

import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardSection;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

/**
 * @author Vaadin Ltd
 */
@Route("vaadin-dashboard/drag-resize")
public class DashboardDragResizePage extends Div {

    public DashboardDragResizePage() {
        Dashboard dashboard = new Dashboard();
        dashboard.setEditable(true);
        dashboard.setMinimumRowHeight("200px");
        dashboard.setMinimumColumnWidth("250px");
        dashboard.setMaximumColumnWidth("250px");

        DashboardWidget widget = new DashboardWidget();
        widget.setTitle("Widget");
        dashboard.add(widget);

        DashboardWidget widgetInSection = new DashboardWidget();
        widgetInSection.setTitle("Widget in section");
        DashboardSection section = dashboard.addSection("Section");
        section.add(widgetInSection);

        NativeButton toggleEditable = new NativeButton("Toggle editable",
                e -> dashboard.setEditable(!dashboard.isEditable()));
        toggleEditable.setId("toggle-editable");

        add(toggleEditable, dashboard);
    }
}
