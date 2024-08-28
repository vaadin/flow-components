/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard.tests;

import java.util.List;

import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

/**
 * @author Vaadin Ltd
 */
@Route("vaadin-dashboard")
public class DashboardPage extends Div {

    public DashboardPage() {
        DashboardWidget widget1 = new DashboardWidget();
        widget1.setTitle("Widget 1");
        widget1.setId("widget-1");

        DashboardWidget widget2 = new DashboardWidget();
        widget2.setTitle("Widget 2");
        widget2.setId("widget-2");

        DashboardWidget widget3 = new DashboardWidget();
        widget3.setTitle("Widget 3");
        widget3.setId("widget-3");

        Dashboard dashboard = new Dashboard();
        dashboard.add(widget1, widget2, widget3);

        dashboard.setMaximumColumnCount(3);

        NativeButton addWidgetAtIndex1 = new NativeButton(
                "Add widget at index 1");
        addWidgetAtIndex1.addClickListener(click -> {
            DashboardWidget widgetAtIndex1 = new DashboardWidget();
            widgetAtIndex1.setTitle("Widget at index 1");
            widgetAtIndex1.setId("widget-at-index-1");
            dashboard.addWidgetAtIndex(1, widgetAtIndex1);
        });
        addWidgetAtIndex1.setId("add-widget-at-index-1");

        NativeButton removeFirstAndLastWidgets = new NativeButton(
                "Remove first and last widgets");
        removeFirstAndLastWidgets.addClickListener(click -> {
            List<DashboardWidget> currentWidgets = dashboard.getWidgets();
            if (currentWidgets.isEmpty()) {
                return;
            }
            int currentWidgetCount = currentWidgets.size();
            if (currentWidgetCount == 1) {
                dashboard.remove(dashboard.getWidgets().get(0));
            } else {
                dashboard.remove(dashboard.getWidgets().get(0),
                        dashboard.getWidgets().get(currentWidgetCount - 1));
            }
        });
        removeFirstAndLastWidgets.setId("remove-first-and-last-widgets");

        NativeButton removeAllWidgets = new NativeButton("Remove all widgets");
        removeAllWidgets.addClickListener(click -> dashboard.removeAll());
        removeAllWidgets.setId("remove-all-widgets");

        NativeButton setMaximumColumnCount1 = new NativeButton(
                "Set maximum column count 1");
        setMaximumColumnCount1
                .addClickListener(click -> dashboard.setMaximumColumnCount(1));
        setMaximumColumnCount1.setId("set-maximum-column-count-1");

        NativeButton setMaximumColumnCountNull = new NativeButton(
                "Set maximum column count null");
        setMaximumColumnCountNull.addClickListener(
                click -> dashboard.setMaximumColumnCount(null));
        setMaximumColumnCountNull.setId("set-maximum-column-count-null");

        add(addWidgetAtIndex1, removeFirstAndLastWidgets, removeAllWidgets,
                setMaximumColumnCount1, setMaximumColumnCountNull, dashboard);
    }
}
