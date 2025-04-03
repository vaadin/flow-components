/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard.tests;

import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardSection;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

/**
 * @author Vaadin Ltd
 */
@Route("vaadin-dashboard/section")
public class DashboardSectionPage extends Div {

    public DashboardSectionPage() {
        Dashboard dashboard = new Dashboard();

        DashboardWidget widget1InSection1 = new DashboardWidget();
        widget1InSection1.setTitle("Widget 1 in Section 1");
        widget1InSection1.setId("widget-1-in-section-1");

        DashboardWidget widget2InSection1 = new DashboardWidget();
        widget2InSection1.setTitle("Widget 2 in Section 1");
        widget2InSection1.setId("widget-2-in-section-1");

        DashboardWidget widget1InSection2 = new DashboardWidget();
        widget1InSection2.setTitle("Widget 1 in Section 2");
        widget1InSection2.setId("widget-1-in-section-2");

        DashboardSection section1 = new DashboardSection("Section 1");
        section1.add(widget1InSection1, widget2InSection1);
        dashboard.addSection(section1);

        DashboardSection section2 = dashboard.addSection("Section 2");
        section2.add(widget1InSection2);

        NativeButton addWidgetToFirstSection = new NativeButton(
                "Add widget to first section");
        addWidgetToFirstSection.addClickListener(
                click -> getFirstSection(dashboard).ifPresent(section -> {
                    DashboardWidget newWidget = new DashboardWidget();
                    newWidget.setTitle("New widget");
                    section.add(newWidget);
                }));
        addWidgetToFirstSection.setId("add-widget-to-first-section");

        NativeButton removeFirstWidgetFromFirstSection = new NativeButton(
                "Remove first widget from first section");
        removeFirstWidgetFromFirstSection.addClickListener(
                click -> getFirstSection(dashboard).ifPresent(section -> {
                    List<DashboardWidget> currentWidgets = section.getWidgets();
                    if (currentWidgets.isEmpty()) {
                        return;
                    }
                    section.remove(currentWidgets.get(0));
                }));
        removeFirstWidgetFromFirstSection
                .setId("remove-first-widget-from-first-section");

        NativeButton removeAllFromFirstSection = new NativeButton(
                "Remove all from first section");
        removeAllFromFirstSection
                .addClickListener(click -> getFirstSection(dashboard)
                        .ifPresent(DashboardSection::removeAll));
        removeAllFromFirstSection.setId("remove-all-from-first-section");

        add(addWidgetToFirstSection, removeFirstWidgetFromFirstSection,
                removeAllFromFirstSection, dashboard);
    }

    private static Optional<DashboardSection> getFirstSection(
            Dashboard dashboard) {
        return dashboard.getChildren()
                .filter(DashboardSection.class::isInstance)
                .map(DashboardSection.class::cast).findFirst();
    }
}
