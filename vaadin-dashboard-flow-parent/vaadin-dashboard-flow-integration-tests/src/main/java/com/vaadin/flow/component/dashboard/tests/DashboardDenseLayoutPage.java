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
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

/**
 * @author Vaadin Ltd
 */
@Route("vaadin-dashboard/dense-layout")
public class DashboardDenseLayoutPage extends Div {

    public DashboardDenseLayoutPage() {
        Dashboard dashboard = new Dashboard();

        DashboardWidget widget1 = new DashboardWidget();
        widget1.setTitle("Widget 1");

        DashboardWidget widget2 = new DashboardWidget();
        widget2.setTitle("Widget 2");
        widget2.setColspan(3);

        DashboardWidget widget3 = new DashboardWidget();
        widget3.setTitle("Widget 3");

        DashboardWidget widget4 = new DashboardWidget();
        widget4.setTitle("Widget 4");

        dashboard.add(widget1, widget2, widget3, widget4);

        NativeButton toggleDenseLayout = new NativeButton("Toggle dense layout",
                e -> dashboard.setDenseLayout(!dashboard.isDenseLayout()));
        toggleDenseLayout.setId("toggle-dense-layout");

        add(toggleDenseLayout, dashboard);
    }
}
