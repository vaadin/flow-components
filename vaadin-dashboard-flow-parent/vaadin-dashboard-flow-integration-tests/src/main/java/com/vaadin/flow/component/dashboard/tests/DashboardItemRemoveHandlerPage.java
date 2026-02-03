/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard.tests;

import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

/**
 * @author Vaadin Ltd
 */
@Route("vaadin-dashboard/item-remove-handler")
public class DashboardItemRemoveHandlerPage extends Div {

    public DashboardItemRemoveHandlerPage() {
        Dashboard dashboard = new Dashboard();
        dashboard.setEditable(true);
        dashboard.setMinimumRowHeight("100px");
        dashboard.setMaximumColumnWidth("400px");

        DashboardWidget widget1 = new DashboardWidget();
        widget1.setTitle("Widget 1");

        DashboardWidget widget2 = new DashboardWidget();
        widget2.setTitle("Widget 2");

        DashboardWidget widget3 = new DashboardWidget();
        widget3.setTitle("Widget 3");

        dashboard.add(widget1, widget2, widget3);

        NativeButton setRemoveHandler = new NativeButton("Set remove handler",
                e -> dashboard.setItemRemoveHandler(event -> {
                    ConfirmDialog dialog = new ConfirmDialog();
                    dialog.setHeader("Confirm removal");
                    dialog.setText(
                            "Are you sure you want to remove this widget?");
                    dialog.setCancelable(true);
                    dialog.addConfirmListener(
                            confirmEvent -> event.removeItem());
                    dialog.open();
                }));
        setRemoveHandler.setId("set-remove-handler");

        add(setRemoveHandler, dashboard);
    }
}
