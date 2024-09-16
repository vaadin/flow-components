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

        DashboardWidget smallWidget = new DashboardWidget();
        smallWidget.setTitle("Small widget");

        DashboardWidget largeWidget = new DashboardWidget();
        largeWidget.setTitle("Large widget");
        largeWidget.setColspan(2);
        largeWidget.setRowspan(2);

        dashboard.add(smallWidget, largeWidget);

        DashboardWidget smallWidgetInSection = new DashboardWidget();
        smallWidgetInSection.setTitle("Small widget in section");

        DashboardWidget largeWidgetInSection = new DashboardWidget();
        largeWidgetInSection.setTitle("Large widget in section");
        largeWidgetInSection.setColspan(2);
        largeWidgetInSection.setRowspan(2);

        DashboardSection section = dashboard.addSection("Section");
        section.add(smallWidgetInSection, largeWidgetInSection);

        NativeButton toggleEditable = new NativeButton("Toggle editable",
                e -> dashboard.setEditable(!dashboard.isEditable()));
        toggleEditable.setId("toggle-editable");

        add(toggleEditable, dashboard);
    }
}
