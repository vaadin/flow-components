/**
 * Copyright 2000-2024 Vaadin Ltd.
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

import com.vaadin.flow.component.dashboard.testbench.DashboardWidgetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * @author Vaadin Ltd
 */
@TestPath("vaadin-dashboard-widget")
public class DashboardWidgetIT extends AbstractComponentIT {

    private DashboardWidgetElement widget;

    @Before
    public void init() {
        open();
        widget = $(DashboardWidgetElement.class).waitForFirst();
    }

    @Test
    public void titleIsSetCorrectly() {
        Assert.assertEquals("Widget", widget.getTitle());
    }
}
