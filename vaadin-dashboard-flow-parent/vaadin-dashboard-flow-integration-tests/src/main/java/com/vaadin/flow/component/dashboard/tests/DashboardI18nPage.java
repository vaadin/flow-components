/**
 * Copyright 2000-2026 Vaadin Ltd.
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

@Route("vaadin-dashboard/i18n")
public class DashboardI18nPage extends Div {

    public DashboardI18nPage() {
        Dashboard dashboard = new Dashboard();
        dashboard.setEditable(true);

        dashboard.add(new DashboardWidget());
        DashboardSection section = dashboard.addSection();
        section.add(new DashboardWidget());

        NativeButton setCustomI18n = new NativeButton("Set custom i18n",
                e -> dashboard.setI18n(getCustomI18n()));
        setCustomI18n.setId("set-custom-i18n");

        NativeButton setEmptyI18n = new NativeButton("Set empty i18n",
                e -> dashboard.setI18n(new Dashboard.DashboardI18n()));
        setEmptyI18n.setId("set-empty-i18n");

        add(setCustomI18n, setEmptyI18n, dashboard);
    }

    private static Dashboard.DashboardI18n getCustomI18n() {
        return new Dashboard.DashboardI18n().setRemove("Custom remove")
                .setMove("Custom move").setResize("Custom resize");
    }
}
