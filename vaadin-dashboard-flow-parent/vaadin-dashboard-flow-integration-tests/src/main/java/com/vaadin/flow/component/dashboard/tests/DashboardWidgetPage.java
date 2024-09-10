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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

/**
 * @author Vaadin Ltd
 */
@Route("vaadin-dashboard-widget")
public class DashboardWidgetPage extends Div {

    public DashboardWidgetPage() {
        Dashboard dashboard = new Dashboard();

        DashboardWidget widget1 = new DashboardWidget();
        widget1.setTitle("Widget 1");
        widget1.setContent(new Div("Some content"));
        widget1.setHeader(new Span("Some header"));
        widget1.setId("widget-1");

        DashboardWidget widget2 = new DashboardWidget();
        widget2.setTitle("Widget 2");
        widget2.setId("widget-2");

        dashboard.add(widget1, widget2);

        NativeButton increaseAllColspansBy1 = new NativeButton(
                "Increase all colspans by 1");
        increaseAllColspansBy1.addClickListener(click -> dashboard.getWidgets()
                .forEach(widget -> widget.setColspan(widget.getColspan() + 1)));
        increaseAllColspansBy1.setId("increase-all-colspans-by-1");

        NativeButton decreaseAllColspansBy1 = new NativeButton(
                "Decrease all colspans by 1");
        decreaseAllColspansBy1.addClickListener(click -> dashboard.getWidgets()
                .forEach(widget -> widget.setColspan(widget.getColspan() - 1)));
        decreaseAllColspansBy1.setId("decrease-all-colspans-by-1");

        NativeButton increaseAllRowspansBy1 = new NativeButton(
                "Increase all rowspans by 1");
        increaseAllRowspansBy1.addClickListener(click -> dashboard.getWidgets()
                .forEach(widget -> widget.setRowspan(widget.getRowspan() + 1)));
        increaseAllRowspansBy1.setId("increase-all-rowspans-by-1");

        NativeButton decreaseAllRowspansBy1 = new NativeButton(
                "Decrease all rowspans by 1");
        decreaseAllRowspansBy1.addClickListener(click -> dashboard.getWidgets()
                .forEach(widget -> widget.setRowspan(widget.getRowspan() - 1)));
        decreaseAllRowspansBy1.setId("decrease-all-rowspans-by-1");

        NativeButton updateContentOfTheFirstWidget = new NativeButton(
                "Update content of the first widget");
        updateContentOfTheFirstWidget.addClickListener(click -> {
            List<DashboardWidget> widgets = dashboard.getWidgets();
            if (!widgets.isEmpty()) {
                widgets.get(0).setContent(new Span("Updated content"));
            }
        });
        updateContentOfTheFirstWidget
                .setId("update-content-of-the-first-widget");

        NativeButton removeContentOfTheFirstWidget = new NativeButton(
                "Remove content of the first widget");
        removeContentOfTheFirstWidget.addClickListener(click -> {
            List<DashboardWidget> widgets = dashboard.getWidgets();
            if (!widgets.isEmpty()) {
                widgets.get(0).setContent(null);
            }
        });
        removeContentOfTheFirstWidget
                .setId("remove-content-of-the-first-widget");

        add(updateContentOfTheFirstWidget, removeContentOfTheFirstWidget,
                increaseAllColspansBy1, decreaseAllColspansBy1,
                increaseAllRowspansBy1, decreaseAllRowspansBy1, dashboard);
    }
}
