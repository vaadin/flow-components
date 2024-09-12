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
@Route("vaadin-dashboard/drag-drop")
public class DashboardDragDropPage extends Div {

    public DashboardDragDropPage() {
        Dashboard dashboard = new Dashboard();
        dashboard.setEditable(true);

        DashboardWidget widget1 = new DashboardWidget();
        widget1.setTitle("Widget 1");

        DashboardWidget widget2 = new DashboardWidget();
        widget2.setTitle("Widget 2");

        dashboard.add(widget1, widget2);

        DashboardWidget widget1InSection1 = new DashboardWidget();
        widget1InSection1.setTitle("Widget 1 in Section 1");

        DashboardWidget widget2InSection1 = new DashboardWidget();
        widget2InSection1.setTitle("Widget 2 in Section 1");

        DashboardSection section1 = new DashboardSection("Section 1");
        section1.add(widget1InSection1, widget2InSection1);

        dashboard.addSection(section1);

        DashboardWidget widgetInSection2 = new DashboardWidget();
        widgetInSection2.setTitle("Widget in Section 2");

        DashboardSection section2 = new DashboardSection("Section 2");
        section2.add(widgetInSection2);

        dashboard.addSection(section2);

        NativeButton toggleAttached = new NativeButton("Toggle attached", e -> {
            if (dashboard.getParent().isPresent()) {
                dashboard.removeFromParent();
            } else {
                add(dashboard);
            }
        });
        toggleAttached.setId("toggle-attached");

        add(toggleAttached, dashboard);
    }
}
