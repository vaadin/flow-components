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
@Route("vaadin-dashboard")
public class DashboardPage extends Div {

    public DashboardPage() {
        Dashboard dashboard = new Dashboard();

        DashboardWidget widget1 = new DashboardWidget();
        widget1.setTitle("Widget 1");
        widget1.setId("widget-1");

        DashboardWidget widget2 = new DashboardWidget();
        widget2.setTitle("Widget 2");
        widget2.setId("widget-2");

        DashboardWidget widget3 = new DashboardWidget();
        widget3.setTitle("Widget 3");
        widget3.setId("widget-3");

        DashboardWidget widget1InSection1 = new DashboardWidget();
        widget1InSection1.setTitle("Widget 1 in Section 1");
        widget1InSection1.setId("widget-1-in-section-1");

        DashboardWidget widget2InSection1 = new DashboardWidget();
        widget2InSection1.setTitle("Widget 2 in Section 1");
        widget2InSection1.setId("widget-2-in-section-1");

        DashboardWidget widget1InSection2 = new DashboardWidget();
        widget1InSection2.setTitle("Widget 1 in Section 2");
        widget1InSection2.setId("widget-1-in-section-2");

        dashboard.add(widget1, widget2, widget3);

        DashboardSection section1 = new DashboardSection("Section 1");
        section1.add(widget1InSection1, widget2InSection1);
        dashboard.addSection(section1);

        DashboardSection section2 = dashboard.addSection("Section 2");
        section2.add(widget1InSection2);

        NativeButton addMultipleWidgets = new NativeButton(
                "Add multiple widgets");
        addMultipleWidgets.addClickListener(click -> {
            DashboardWidget newWidget1 = new DashboardWidget();
            newWidget1.setTitle("New widget 1");
            DashboardWidget newWidget2 = new DashboardWidget();
            newWidget2.setTitle("New widget 2");
            dashboard.add(newWidget2);
            dashboard.addWidgetAtIndex(
                    (int) (dashboard.getChildren().count() - 1), newWidget1);
        });
        addMultipleWidgets.setId("add-multiple-widgets");

        NativeButton removeFirstAndLastWidgets = new NativeButton(
                "Remove first and last widgets");
        removeFirstAndLastWidgets.addClickListener(click -> {
            List<DashboardWidget> currentWidgets = dashboard.getWidgets();
            if (currentWidgets.isEmpty()) {
                return;
            }
            int currentWidgetCount = currentWidgets.size();
            if (currentWidgetCount == 1) {
                dashboard.getWidgets().get(0).removeFromParent();
            } else {
                dashboard.getWidgets().get(currentWidgetCount - 1)
                        .removeFromParent();
                dashboard.getWidgets().get(0).removeFromParent();
            }
        });
        removeFirstAndLastWidgets.setId("remove-first-and-last-widgets");

        NativeButton removeAll = new NativeButton("Remove all");
        removeAll.addClickListener(click -> dashboard.removeAll());
        removeAll.setId("remove-all");

        NativeButton addSectionWithMultipleWidgets = new NativeButton(
                "Add section with multiple widgets");
        addSectionWithMultipleWidgets.addClickListener(click -> {
            DashboardSection section = dashboard
                    .addSection("New section with multiple widgets");
            DashboardWidget newWidget1 = new DashboardWidget();
            newWidget1.setTitle("New widget 1");
            DashboardWidget newWidget2 = new DashboardWidget();
            newWidget2.setTitle("New widget 2");
            section.add(newWidget2);
            section.addWidgetAtIndex(0, newWidget1);
        });
        addSectionWithMultipleWidgets
                .setId("add-section-with-multiple-widgets");

        NativeButton removeFirstSection = new NativeButton(
                "Remove first section");
        removeFirstSection.addClickListener(click -> getFirstSection(dashboard)
                .ifPresent(dashboard::remove));
        removeFirstSection.setId("remove-first-section");

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

        add(addMultipleWidgets, removeFirstAndLastWidgets, removeAll,
                addSectionWithMultipleWidgets, removeFirstSection,
                addWidgetToFirstSection, removeFirstWidgetFromFirstSection,
                removeAllFromFirstSection, setMaximumColumnCount1,
                setMaximumColumnCountNull, increaseAllColspansBy1,
                decreaseAllColspansBy1, dashboard);
    }

    private static Optional<DashboardSection> getFirstSection(
            Dashboard dashboard) {
        return dashboard.getChildren()
                .filter(DashboardSection.class::isInstance)
                .map(DashboardSection.class::cast).findFirst();
    }
}
