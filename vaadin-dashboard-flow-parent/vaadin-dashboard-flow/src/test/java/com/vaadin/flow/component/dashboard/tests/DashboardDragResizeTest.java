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

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardItemResizedEvent;
import com.vaadin.flow.component.dashboard.DashboardSection;
import com.vaadin.flow.component.dashboard.DashboardWidget;

public class DashboardDragResizeTest extends DashboardTestBase {
    private Dashboard dashboard;

    @Before
    @Override
    public void setup() {
        super.setup();
        dashboard = new Dashboard();
        dashboard.add(new DashboardWidget());
        dashboard.setEditable(true);
        DashboardSection section = dashboard.addSection();
        section.add(new DashboardWidget());
        getUi().add(dashboard);
        fakeClientCommunication();
    }

    @Test
    public void resizeWidgetHorizontally_sizeIsUpdated() {
        assertWidgetResized(0, 2, 1);
    }

    @Test
    public void resizeWidgetVertically_sizeIsUpdated() {
        assertWidgetResized(0, 1, 2);
    }

    @Test
    public void resizeWidgetBothHorizontallyAndVertically_sizeIsUpdated() {
        assertWidgetResized(0, 2, 2);
    }

    @Test
    public void resizeWidgetInSectionHorizontally_sizeIsUpdated() {
        assertWidgetResized(1, 2, 1);
    }

    @Test
    public void resizeWidgetInSectionVertically_sizeIsUpdated() {
        assertWidgetResized(1, 1, 2);
    }

    @Test
    public void resizeWidgetInSectionBothHorizontallyAndVertically_sizeIsUpdated() {
        assertWidgetResized(1, 2, 2);
    }

    @Test
    public void setDashboardNotEditable_resizeWidget_sizeIsNotUpdated() {
        dashboard.setEditable(false);
        DashboardWidget widgetToResize = dashboard.getWidgets().get(0);
        fireItemResizedEvent(widgetToResize, 2, 2);
        Assert.assertEquals(1, widgetToResize.getColspan());
        Assert.assertEquals(1, widgetToResize.getRowspan());
    }

    private void assertWidgetResized(int widgetIndexToResize, int targetColspan,
            int targetRowspan) {
        DashboardWidget widgetToResize = dashboard.getWidgets()
                .get(widgetIndexToResize);
        // Assert widget is enlarged
        fireItemResizedEvent(widgetToResize, targetColspan, targetRowspan);
        Assert.assertEquals(targetColspan, widgetToResize.getColspan());
        Assert.assertEquals(targetRowspan, widgetToResize.getRowspan());
        // Assert widget is shrunk
        fireItemResizedEvent(widgetToResize, 1, 1);
        Assert.assertEquals(1, widgetToResize.getColspan());
        Assert.assertEquals(1, widgetToResize.getRowspan());
    }

    private void fireItemResizedEvent(DashboardWidget widget, int targetColspan,
            int targetRowspan) {
        ComponentUtil.fireEvent(dashboard,
                new DashboardItemResizedEvent(dashboard, false,
                        widget.getElement().getNode().getId(), targetColspan,
                        targetRowspan));
    }
}
