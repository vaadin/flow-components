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
import com.vaadin.flow.router.Route;

/**
 * @author Vaadin Ltd
 */
@Route("vaadin-dashboard/item-resize")
public class DashboardItemResizePage extends Div {

    public DashboardItemResizePage() {
        Dashboard dashboard = new Dashboard();
        dashboard.setEditable(true);
        dashboard.setMinimumRowHeight("200px");
        dashboard.setMinimumColumnWidth("250px");
        dashboard.setMaximumColumnWidth("250px");
        dashboard.setGap("0px");
        dashboard.setPadding("0px");

        DashboardWidget widget = new DashboardWidget();
        widget.setTitle("Widget");
        dashboard.add(widget);

        DashboardWidget widgetInSection = new DashboardWidget();
        widgetInSection.setTitle("Widget in section");
        DashboardSection section = dashboard.addSection("Section");
        section.add(widgetInSection);

        add(dashboard);
    }
}
