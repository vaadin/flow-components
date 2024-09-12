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

import com.vaadin.flow.component.dashboard.testbench.DashboardElement;
import com.vaadin.flow.component.dashboard.testbench.DashboardSectionElement;
import com.vaadin.flow.component.dashboard.testbench.DashboardWidgetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * @author Vaadin Ltd
 */
@TestPath("vaadin-dashboard/drag-drop")
public class DashboardDragDropIT extends AbstractComponentIT {

    private DashboardElement dashboardElement;

    @Before
    public void init() {
        open();
        dashboardElement = $(DashboardElement.class).waitForFirst();
    }

    @Test
    public void reorderWidgetOnClientSide_itemsAreReorderedCorrectly() {
        DashboardWidgetElement widgetToReorder = dashboardElement.getWidgets()
                .get(0);
        reorderRootLevelItem(1, 0);
        Assert.assertEquals(widgetToReorder.getTitle(),
                dashboardElement.getWidgets().get(1).getTitle());
    }

    @Test
    public void reorderSectionOnClientSide_itemsAreReorderedCorrectly() {
        DashboardSectionElement sectionToReorder = dashboardElement
                .getSections().get(0);
        reorderRootLevelItem(2, 3);
        Assert.assertEquals(sectionToReorder.getTitle(),
                dashboardElement.getSections().get(1).getTitle());
    }

    @Test
    public void reorderWidgetInSectionOnClientSide_itemsAreReorderedCorrectly() {
        DashboardSectionElement firstSection = dashboardElement.getSections()
                .get(0);
        DashboardWidgetElement widgetToReorder = firstSection.getWidgets()
                .get(1);
        reorderWidgetInSection(2, 0, 1);
        firstSection = dashboardElement.getSections().get(0);
        Assert.assertEquals(widgetToReorder.getTitle(),
                firstSection.getWidgets().get(0).getTitle());
    }

    @Test
    public void detachReattach_reorderWidgetOnClientSide_itemsAreReorderedCorrectly() {
        clickElementWithJs("toggle-attached");
        clickElementWithJs("toggle-attached");
        dashboardElement = $(DashboardElement.class).waitForFirst();
        reorderWidgetOnClientSide_itemsAreReorderedCorrectly();
    }

    private void reorderWidgetInSection(int sectionIndex, int initialIndex,
            int targetIndex) {
        executeScript(
                """
                        const sectionIndex = %d;
                        const reorderedItem = arguments[0].items[sectionIndex].items.splice(%d, 1)[0];
                        arguments[0].items[sectionIndex].items.splice(%d, 0, reorderedItem);
                        arguments[0].dispatchEvent(new CustomEvent('dashboard-item-reorder-end'));"""
                        .formatted(sectionIndex, initialIndex, targetIndex),
                dashboardElement);
    }

    private void reorderRootLevelItem(int initialIndex, int targetIndex) {
        executeScript(
                """
                        const reorderedItem = arguments[0].items.splice(%d, 1)[0];
                        arguments[0].items.splice(%d, 0, reorderedItem);
                        arguments[0].dispatchEvent(new CustomEvent('dashboard-item-reorder-end'));"""
                        .formatted(initialIndex, targetIndex),
                dashboardElement);
    }
}
