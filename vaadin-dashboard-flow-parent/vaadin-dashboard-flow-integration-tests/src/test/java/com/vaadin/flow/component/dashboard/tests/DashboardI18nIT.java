/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.dashboard.testbench.DashboardElement;
import com.vaadin.flow.component.dashboard.testbench.DashboardWidgetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the {@link DashboardI18nPage}.
 *
 * @author Vaadin Ltd.
 */
@TestPath("vaadin-dashboard/i18n")
public class DashboardI18nIT extends AbstractComponentIT {

    private DashboardElement dashboardElement;

    @Before
    public void init() {
        open();
        dashboardElement = $(DashboardElement.class).waitForFirst();
    }

    @Test
    public void setI18n_i18nIsApplied() {
        clickElementWithJs("set-i18n");

        DashboardWidgetElement widget = dashboardElement.getWidgets().get(0);

        Assert.assertEquals("Custom remove",
                getButtonTitle(widget, "remove-button"));
        Assert.assertEquals("Custom move",
                getButtonTitle(widget, "drag-handle"));
        Assert.assertEquals("Custom resize",
                getButtonTitle(widget, "resize-handle"));
    }

    @Test
    public void setI18n_setEmptyI18n_defaultI18nIsRestored() {
        clickElementWithJs("set-i18n");
        clickElementWithJs("set-empty-i18n");

        DashboardWidgetElement widget = dashboardElement.getWidgets().get(0);

        Assert.assertEquals("Remove", getButtonTitle(widget, "remove-button"));
        Assert.assertEquals("Move", getButtonTitle(widget, "drag-handle"));
        Assert.assertEquals("Resize", getButtonTitle(widget, "resize-handle"));
    }

    private String getButtonTitle(DashboardWidgetElement widget,
            String buttonId) {
        TestBenchElement button = widget.$("vaadin-dashboard-button")
                .withId(buttonId).first();
        return button.getAttribute("title");
    }
}
