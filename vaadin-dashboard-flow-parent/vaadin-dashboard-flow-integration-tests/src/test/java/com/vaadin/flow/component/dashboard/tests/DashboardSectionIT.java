/**
 * Copyright 2000-2025 Vaadin Ltd.
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

import com.vaadin.flow.component.dashboard.testbench.DashboardElement;
import com.vaadin.flow.component.dashboard.testbench.DashboardSectionElement;
import com.vaadin.flow.component.dashboard.testbench.DashboardWidgetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * @author Vaadin Ltd
 */
@TestPath("vaadin-dashboard/section")
public class DashboardSectionIT extends AbstractComponentIT {

    private DashboardElement dashboardElement;

    @Before
    public void init() {
        open();
        dashboardElement = $(DashboardElement.class).waitForFirst();
    }

    @Test
    public void addWidgetToFirstSection_widgetsAreAdded() {
        clickElementWithJs("add-widget-to-first-section");
        List<DashboardSectionElement> sections = dashboardElement.getSections();
        DashboardSectionElement firstSection = sections.get(0);
        Assert.assertEquals("Section 1", firstSection.getTitle());
        assertSectionWidgetsByTitle(firstSection, "Widget 1 in Section 1",
                "Widget 2 in Section 1", "New widget");
    }

    @Test
    public void removeFirstWidgetFromFirstSection_widgetIsRemoved() {
        clickElementWithJs("remove-first-widget-from-first-section");
        List<DashboardSectionElement> sections = dashboardElement.getSections();
        DashboardSectionElement firstSection = sections.get(0);
        Assert.assertEquals("Section 1", firstSection.getTitle());
        assertSectionWidgetsByTitle(firstSection, "Widget 2 in Section 1");
    }

    @Test
    public void removeAllFromFirstSection_widgetsAreRemoved() {
        clickElementWithJs("remove-all-from-first-section");
        List<DashboardSectionElement> sections = dashboardElement.getSections();
        DashboardSectionElement firstSection = sections.get(0);
        Assert.assertEquals("Section 1", firstSection.getTitle());
        assertSectionWidgetsByTitle(firstSection);
    }

    private static void assertSectionWidgetsByTitle(
            DashboardSectionElement section, String... expectedWidgetTitles) {
        assertWidgetsByTitle(section.getWidgets(), expectedWidgetTitles);
    }

    private static void assertWidgetsByTitle(
            List<DashboardWidgetElement> actualWidgets,
            String... expectedWidgetTitles) {
        List<String> widgetTitles = actualWidgets.stream()
                .map(DashboardWidgetElement::getTitle).toList();
        Assert.assertEquals(Arrays.asList(expectedWidgetTitles), widgetTitles);
    }
}
