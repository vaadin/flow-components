/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard.tests;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Dimension;

import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.component.dashboard.testbench.DashboardElement;
import com.vaadin.flow.component.dashboard.testbench.DashboardWidgetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * @author Vaadin Ltd
 */
@TestPath("vaadin-dashboard/item-remove-handler")
public class DashboardItemRemoveHandlerIT extends AbstractComponentIT {

    private DashboardElement dashboardElement;

    @Before
    public void init() {
        open();
        getDriver().manage().window().setSize(new Dimension(1920, 1080));
        dashboardElement = $(DashboardElement.class).waitForFirst();
    }

    @Test
    public void withoutRemoveHandler_clickRemoveButton_itemIsRemovedImmediately() {
        assertDashboardWidgetsByTitle("Widget 1", "Widget 2", "Widget 3");

        DashboardWidgetElement widgetToRemove = dashboardElement.getWidgets()
                .get(0);
        getRemoveButton(widgetToRemove).click();

        assertDashboardWidgetsByTitle("Widget 2", "Widget 3");
    }

    @Test
    public void withRemoveHandler_clickRemoveButton_itemIsNotRemoved() {
        clickElementWithJs("set-remove-handler");

        assertDashboardWidgetsByTitle("Widget 1", "Widget 2", "Widget 3");

        DashboardWidgetElement widgetToRemove = dashboardElement.getWidgets()
                .get(0);
        getRemoveButton(widgetToRemove).click();

        assertDashboardWidgetsByTitle("Widget 1", "Widget 2", "Widget 3");
    }

    @Test
    public void withRemoveHandler_clickRemoveButton_confirmRemoval_itemIsRemoved() {
        clickElementWithJs("set-remove-handler");

        assertDashboardWidgetsByTitle("Widget 1", "Widget 2", "Widget 3");

        DashboardWidgetElement widgetToRemove = dashboardElement.getWidgets()
                .get(0);
        getRemoveButton(widgetToRemove).click();

        ConfirmDialogElement confirmDialog = $(ConfirmDialogElement.class)
                .waitForFirst();
        Assert.assertNotNull(confirmDialog);

        confirmDialog.getConfirmButton().click();

        assertDashboardWidgetsByTitle("Widget 2", "Widget 3");
    }

    private void assertDashboardWidgetsByTitle(String... expectedWidgetTitles) {
        List<String> widgetTitles = dashboardElement.getWidgets().stream()
                .map(DashboardWidgetElement::getTitle).toList();
        Assert.assertEquals(Arrays.asList(expectedWidgetTitles), widgetTitles);
    }

    private static TestBenchElement getRemoveButton(TestBenchElement element) {
        return element.$("vaadin-dashboard-button").withId("remove-button")
                .first();
    }
}
