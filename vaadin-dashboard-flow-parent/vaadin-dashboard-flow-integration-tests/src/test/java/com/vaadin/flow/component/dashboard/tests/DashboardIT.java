/**
 * Copyright 2000-2024 Vaadin Ltd.
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

import com.vaadin.flow.component.dashboard.testbench.DashboardElement;
import com.vaadin.flow.component.dashboard.testbench.DashboardSectionElement;
import com.vaadin.flow.component.dashboard.testbench.DashboardWidgetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * @author Vaadin Ltd
 */
@TestPath("vaadin-dashboard")
public class DashboardIT extends AbstractComponentIT {

    private DashboardElement dashboardElement;

    @Before
    public void init() {
        open();
        getDriver().manage().window().setSize(new Dimension(1920, 1080));
        dashboardElement = $(DashboardElement.class).waitForFirst();
    }

    @Test
    public void addInitialWidgetsToDashboard_widgetsAreCorrectlyAdded() {
        assertDashboardWidgetsByTitle("Widget 1", "Widget 2", "Widget 3",
                "Widget 1 in Section 1", "Widget 2 in Section 1",
                "Widget 1 in Section 2");
    }

    @Test
    public void addWidgetsToDashboard_widgetsAreCorrectlyAdded() {
        clickElementWithJs("add-multiple-widgets");
        assertDashboardWidgetsByTitle("Widget 1", "Widget 2", "Widget 3",
                "Widget 1 in Section 1", "Widget 2 in Section 1",
                "Widget 1 in Section 2", "New widget 1", "New widget 2");
    }

    @Test
    public void removeFirstAndLastWidgetsFromDashboard_widgetsAreCorrectlyRemoved() {
        clickElementWithJs("remove-first-and-last-widgets");
        assertDashboardWidgetsByTitle("Widget 2", "Widget 3",
                "Widget 1 in Section 1", "Widget 2 in Section 1");
    }

    @Test
    public void removeWidgetUsingButton_widgetIsRemoved() {
        clickElementWithJs("toggle-editable");
        DashboardWidgetElement widgetToRemove = dashboardElement.getWidgets()
                .get(0);
        getRemoveButton(widgetToRemove).click();
        assertDashboardWidgetsByTitle("Widget 2", "Widget 3",
                "Widget 1 in Section 1", "Widget 2 in Section 1",
                "Widget 1 in Section 2");
    }

    @Test
    public void removeWidgetInSectionUsingButton_widgetIsRemoved() {
        clickElementWithJs("toggle-editable");
        DashboardSectionElement section = dashboardElement.getSections().get(0);
        DashboardWidgetElement widgetToRemove = section.getWidgets().get(0);
        getRemoveButton(widgetToRemove).click();
        assertDashboardWidgetsByTitle("Widget 1", "Widget 2", "Widget 3",
                "Widget 2 in Section 1", "Widget 1 in Section 2");
    }

    @Test
    public void removeAllFromDashboard_widgetsAnsSectionsAreCorrectlyRemoved() {
        clickElementWithJs("remove-all");
        assertDashboardWidgetsByTitle();
        Assert.assertTrue(dashboardElement.getSections().isEmpty());
    }

    @Test
    public void addInitialSectionsWithWidgetsToDashboard_widgetsAreCorrectlyAdded() {
        List<DashboardSectionElement> sections = dashboardElement.getSections();
        Assert.assertEquals(2, sections.size());

        DashboardSectionElement section1 = sections.get(0);
        Assert.assertEquals("Section 1", section1.getTitle());
        assertSectionWidgetsByTitle(section1, "Widget 1 in Section 1",
                "Widget 2 in Section 1");

        DashboardSectionElement section2 = sections.get(1);
        Assert.assertEquals("Section 2", section2.getTitle());
        assertSectionWidgetsByTitle(section2, "Widget 1 in Section 2");
    }

    @Test
    public void addSectionWithWidgets_sectionAndWidgetsAreCorrectlyAdded() {
        clickElementWithJs("add-section-with-multiple-widgets");
        List<DashboardSectionElement> sections = dashboardElement.getSections();
        Assert.assertEquals(3, sections.size());
        DashboardSectionElement newSection = sections.get(2);
        Assert.assertEquals("New section with multiple widgets",
                newSection.getTitle());
        assertSectionWidgetsByTitle(newSection, "New widget 1", "New widget 2");
    }

    @Test
    public void removeFirstSection_sectionIsRemoved() {
        clickElementWithJs("remove-first-section");
        List<DashboardSectionElement> sections = dashboardElement.getSections();
        Assert.assertEquals(1, sections.size());
        DashboardSectionElement firstSection = sections.get(0);
        Assert.assertEquals("Section 2", firstSection.getTitle());
        assertSectionWidgetsByTitle(firstSection, "Widget 1 in Section 2");
    }

    @Test
    public void removeFirstSectionUsingButton_sectionIsRemoved() {
        clickElementWithJs("toggle-editable");
        DashboardSectionElement sectionToRemove = dashboardElement.getSections()
                .get(0);
        String sectionTitle = sectionToRemove.getTitle();
        getRemoveButton(sectionToRemove).click();
        assertDashboardWidgetsByTitle("Widget 1", "Widget 2", "Widget 3",
                "Widget 1 in Section 2");
        List<String> sectionTitles = dashboardElement.getSections().stream()
                .map(DashboardSectionElement::getTitle).toList();
        Assert.assertFalse(sectionTitles.contains(sectionTitle));
    }

    @Test
    public void changeMaximumColumnCountTo1_widgetsShouldBeOnTheSameColumn() {
        List<DashboardWidgetElement> widgets = dashboardElement.getWidgets();
        // The first two widgets should initially be on the same horizontal line
        int yOfWidget1 = widgets.get(0).getLocation().getY();
        Assert.assertEquals(yOfWidget1, widgets.get(1).getLocation().getY());

        clickElementWithJs("set-maximum-column-count-1");
        // The first two widgets should be on the same vertical line
        int xOfWidget1 = widgets.get(0).getLocation().getX();
        Assert.assertEquals(xOfWidget1, widgets.get(1).getLocation().getX());
    }

    @Test
    public void changeMaximumColumnCountToNull_widgetsShouldBeOnTheSameRow() {
        clickElementWithJs("set-maximum-column-count-1");
        List<DashboardWidgetElement> widgets = dashboardElement.getWidgets();
        // The first two widgets should be on the same vertical line
        int xOfWidget1 = widgets.get(0).getLocation().getX();
        Assert.assertEquals(xOfWidget1, widgets.get(1).getLocation().getX());

        clickElementWithJs("set-maximum-column-count-null");
        // The widgets should be on the same horizontal line
        int yOfWidget1 = widgets.get(0).getLocation().getY();
        Assert.assertEquals(yOfWidget1, widgets.get(1).getLocation().getY());
    }

    private void assertDashboardWidgetsByTitle(String... expectedWidgetTitles) {
        assertWidgetsByTitle(dashboardElement.getWidgets(),
                expectedWidgetTitles);
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

    private static TestBenchElement getRemoveButton(TestBenchElement element) {
        return element.$("vaadin-dashboard-button").withId("remove-button")
                .first();
    }
}
