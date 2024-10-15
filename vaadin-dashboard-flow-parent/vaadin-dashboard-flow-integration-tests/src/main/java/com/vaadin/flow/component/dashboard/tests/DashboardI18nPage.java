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

@Route("vaadin-dashboard/i18n")
public class DashboardI18nPage extends Div {

    enum I18nEntry {
        SELECT_SECTION("selectSection"),

        SELECT_WIDGET("selectWidget"),

        REMOVE("remove"),

        RESIZE("resize"),

        RESIZE_APPLY("resizeApply"),

        RESIZE_SHRINK_WIDTH("resizeShrinkWidth"),

        RESIZE_GROW_WIDTH("resizeGrowWidth"),

        RESIZE_SHRINK_HEIGHT("resizeShrinkHeight"),

        RESIZE_GROW_HEIGHT("resizeGrowHeight"),

        MOVE("move"),

        MOVE_APPLY("moveApply"),

        MOVE_FORWARD("moveForward"),

        MOVE_BACKWARD("moveBackward");

        private final String key;

        I18nEntry(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public String getCustomValue() {
            return "Custom " + key;
        }
    }

    public DashboardI18nPage() {
        Dashboard dashboard = new Dashboard();
        dashboard.setEditable(true);

        dashboard.add(new DashboardWidget());
        DashboardSection section = dashboard.addSection();
        section.add(new DashboardWidget());

        NativeButton setCustomI18n = new NativeButton("Set custom i18n",
                e -> dashboard.setI18n(getCustomI18n()));
        setCustomI18n.setId("set-custom-i18n");

        add(setCustomI18n, dashboard);
    }

    private static Dashboard.DashboardI18n getCustomI18n() {
        Dashboard.DashboardI18n dashboardI18n = new Dashboard.DashboardI18n();
        dashboardI18n
                .setSelectSection(I18nEntry.SELECT_SECTION.getCustomValue());
        dashboardI18n.setSelectWidget(I18nEntry.SELECT_WIDGET.getCustomValue());
        dashboardI18n.setRemove(I18nEntry.REMOVE.getCustomValue());
        dashboardI18n.setResize(I18nEntry.RESIZE.getCustomValue());
        dashboardI18n.setResizeApply(I18nEntry.RESIZE_APPLY.getCustomValue());
        dashboardI18n.setResizeShrinkWidth(
                I18nEntry.RESIZE_SHRINK_WIDTH.getCustomValue());
        dashboardI18n.setResizeGrowWidth(
                I18nEntry.RESIZE_GROW_WIDTH.getCustomValue());
        dashboardI18n.setResizeShrinkHeight(
                I18nEntry.RESIZE_SHRINK_HEIGHT.getCustomValue());
        dashboardI18n.setResizeGrowHeight(
                I18nEntry.RESIZE_GROW_HEIGHT.getCustomValue());
        dashboardI18n.setMove(I18nEntry.MOVE.getCustomValue());
        dashboardI18n.setMoveApply(I18nEntry.MOVE_APPLY.getCustomValue());
        dashboardI18n.setMoveForward(I18nEntry.MOVE_FORWARD.getCustomValue());
        dashboardI18n.setMoveBackward(I18nEntry.MOVE_BACKWARD.getCustomValue());
        return dashboardI18n;
    }
}
