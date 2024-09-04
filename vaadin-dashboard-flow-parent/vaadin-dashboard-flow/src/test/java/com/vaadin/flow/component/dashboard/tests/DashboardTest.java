/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardSection;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.server.VaadinSession;

public class DashboardTest {
    private final UI ui = new UI();
    private Dashboard dashboard;

    @Before
    public void setup() {
        UI.setCurrent(ui);
        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);
        dashboard = new Dashboard();
        ui.add(dashboard);
        fakeClientCommunication();
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void addWidget_widgetIsAdded() {
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        dashboard.add(widget1, widget2);
        fakeClientCommunication();
        assertChildComponents(dashboard, widget1, widget2);
    }

    @Test
    public void addNullWidget_exceptionIsThrown() {
        Assert.assertThrows(NullPointerException.class,
                () -> dashboard.add((DashboardWidget) null));
    }

    @Test
    public void addNullWidgetInArray_noWidgetIsAdded() {
        DashboardWidget widget = new DashboardWidget();
        try {
            dashboard.add(widget, null);
        } catch (NullPointerException e) {
            // Do nothing
        }
        fakeClientCommunication();
        assertChildComponents(dashboard);
    }

    @Test
    public void addWidgetAtIndex_widgetIsCorrectlyAdded() {
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        DashboardWidget widget3 = new DashboardWidget();
        dashboard.add(widget1, widget2);
        fakeClientCommunication();
        dashboard.addWidgetAtIndex(1, widget3);
        fakeClientCommunication();
        assertChildComponents(dashboard, widget1, widget3, widget2);
    }

    @Test
    public void addWidgetAtInvalidIndex_exceptionIsThrown() {
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        dashboard.add(widget1);
        fakeClientCommunication();
        Assert.assertThrows(IllegalArgumentException.class,
                () -> dashboard.addWidgetAtIndex(2, widget2));
        fakeClientCommunication();
        assertChildComponents(dashboard, widget1);
    }

    @Test
    public void addWidgetAtNegativeIndex_exceptionIsThrown() {
        DashboardWidget widget = new DashboardWidget();
        Assert.assertThrows(IllegalArgumentException.class,
                () -> dashboard.addWidgetAtIndex(-1, widget));
        fakeClientCommunication();
        assertChildComponents(dashboard);
    }

    @Test
    public void addNullWidgetAtIndex_exceptionIsThrown() {
        Assert.assertThrows(NullPointerException.class,
                () -> dashboard.addWidgetAtIndex(0, null));
    }

    @Test
    public void removeWidget_widgetIsRemoved() {
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        dashboard.add(widget1, widget2);
        fakeClientCommunication();
        dashboard.remove(widget1);
        fakeClientCommunication();
        assertChildComponents(dashboard, widget2);
    }

    @Test
    public void removeNullWidget_exceptionIsThrown() {
        Assert.assertThrows(NullPointerException.class,
                () -> dashboard.remove((DashboardWidget) null));
    }

    @Test
    public void removeAllWidgets_widgetsAreRemoved() {
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        dashboard.add(widget1, widget2);
        fakeClientCommunication();
        dashboard.removeAll();
        fakeClientCommunication();
        assertChildComponents(dashboard);
    }

    @Test
    public void removeWidgetFromParent_widgetIsRemoved() {
        DashboardWidget widget1 = new DashboardWidget();
        dashboard.add(widget1);
        fakeClientCommunication();
        widget1.removeFromParent();
        fakeClientCommunication();
        assertChildComponents(dashboard);
    }

    @Test
    public void addMultipleWidgets_removeOneFromParent_widgetIsRemoved() {
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        dashboard.add(widget1, widget2);
        fakeClientCommunication();
        widget1.removeFromParent();
        fakeClientCommunication();
        assertChildComponents(dashboard, widget2);
    }

    @Test
    public void addWidgetsSeparately_removeOneFromParent_widgetIsRemoved() {
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        dashboard.add(widget1);
        dashboard.add(widget2);
        fakeClientCommunication();
        widget1.removeFromParent();
        fakeClientCommunication();
        assertChildComponents(dashboard, widget2);
    }

    @Test
    public void addWidgetFromLayoutToDashboard_widgetIsMoved() {
        Div parent = new Div();
        ui.add(parent);
        DashboardWidget widget = new DashboardWidget();
        parent.add(widget);
        fakeClientCommunication();
        dashboard.add(widget);
        fakeClientCommunication();
        Assert.assertTrue(parent.getChildren().noneMatch(widget::equals));
        assertChildComponents(dashboard, widget);
    }

    @Test
    public void addWidgetFromDashboardToLayout_widgetIsMoved() {
        DashboardWidget widget = new DashboardWidget();
        dashboard.add(widget);
        fakeClientCommunication();
        Div parent = new Div();
        ui.add(parent);
        parent.add(widget);
        fakeClientCommunication();
        assertChildComponents(dashboard);
        Assert.assertTrue(parent.getChildren().anyMatch(widget::equals));
    }

    @Test
    public void addWidgetToAnotherDashboard_widgetIsMoved() {
        DashboardWidget widget = new DashboardWidget();
        dashboard.add(widget);
        fakeClientCommunication();
        Dashboard newDashboard = new Dashboard();
        ui.add(newDashboard);
        newDashboard.add(widget);
        fakeClientCommunication();
        assertChildComponents(dashboard);
        assertChildComponents(newDashboard, widget);
    }

    @Test
    public void addSectionWithoutTitle_sectionIsAdded() {
        DashboardSection section1 = dashboard.addSection();
        DashboardSection section2 = dashboard.addSection();
        fakeClientCommunication();
        assertChildComponents(dashboard, section1, section2);
    }

    @Test
    public void addSectionWithNullTitle_sectionIsAdded() {
        DashboardSection section1 = dashboard.addSection((String) null);
        DashboardSection section2 = dashboard.addSection((String) null);
        fakeClientCommunication();
        assertChildComponents(dashboard, section1, section2);
    }

    @Test
    public void addSectionWithTitle_sectionIsAdded() {
        DashboardSection section1 = dashboard.addSection("Section 1");
        DashboardSection section2 = dashboard.addSection("Section 2");
        fakeClientCommunication();
        assertChildComponents(dashboard, section1, section2);
    }

    @Test
    public void createAndAddSectionWithoutTitle_sectionIsAdded() {
        DashboardSection section1 = new DashboardSection();
        DashboardSection section2 = new DashboardSection();
        dashboard.addSection(section1);
        dashboard.addSection(section2);
        fakeClientCommunication();
        assertChildComponents(dashboard, section1, section2);
    }

    @Test
    public void createAndAddSectionWithNullTitle_sectionIsAdded() {
        DashboardSection section1 = new DashboardSection(null);
        DashboardSection section2 = new DashboardSection(null);
        dashboard.addSection(section1);
        dashboard.addSection(section2);
        fakeClientCommunication();
        assertChildComponents(dashboard, section1, section2);
    }

    @Test
    public void createAndAddSectionWithTitle_sectionIsAdded() {
        DashboardSection section1 = new DashboardSection("Section 1");
        DashboardSection section2 = new DashboardSection("Section 2");
        dashboard.addSection(section1);
        dashboard.addSection(section2);
        fakeClientCommunication();
        assertChildComponents(dashboard, section1, section2);
    }

    @Test
    public void addNullSection_exceptionIsThrown() {
        Assert.assertThrows(NullPointerException.class,
                () -> dashboard.addSection((DashboardSection) null));
    }

    @Test
    public void removeSection_sectionIsRemoved() {
        DashboardSection section1 = dashboard.addSection();
        DashboardSection section2 = dashboard.addSection();
        fakeClientCommunication();
        dashboard.remove(section1);
        fakeClientCommunication();
        assertChildComponents(dashboard, section2);
    }

    @Test
    public void removeNullSection_exceptionIsThrown() {
        Assert.assertThrows(NullPointerException.class,
                () -> dashboard.remove((DashboardSection) null));
    }

    @Test
    public void removeAllSections_sectionsAreRemoved() {
        dashboard.addSection();
        dashboard.addSection();
        fakeClientCommunication();
        dashboard.removeAll();
        fakeClientCommunication();
        assertChildComponents(dashboard);
    }

    @Test
    public void removeSectionFromParent_sectionIsRemoved() {
        DashboardSection section = dashboard.addSection();
        fakeClientCommunication();
        section.removeFromParent();
        fakeClientCommunication();
        assertChildComponents(dashboard);
    }

    @Test
    public void addMultipleSections_removeOneFromParent_sectionIsRemoved() {
        DashboardSection section1 = dashboard.addSection();
        DashboardSection section2 = dashboard.addSection();
        fakeClientCommunication();
        section1.removeFromParent();
        fakeClientCommunication();
        assertChildComponents(dashboard, section2);
    }

    @Test
    public void setTitleOnExistingSection_itemsAreUpdatedWithCorrectTitles() {
        DashboardSection section = dashboard.addSection("Section");
        fakeClientCommunication();
        section.setTitle("New title");
        fakeClientCommunication();
        assertChildComponents(dashboard, section);
    }

    @Test
    public void addSectionWithWidget_removeWidgetFromDashboard_throwsException() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget = new DashboardWidget();
        section.add(widget);
        fakeClientCommunication();
        Assert.assertThrows(IllegalArgumentException.class,
                () -> dashboard.remove(widget));
        fakeClientCommunication();
        assertChildComponents(dashboard, section);
    }

    @Test
    public void addSection_addWidgetToSection_widgetIsAdded() {
        DashboardSection section = dashboard.addSection();
        fakeClientCommunication();
        DashboardWidget widget = new DashboardWidget();
        section.add(widget);
        fakeClientCommunication();
        assertChildComponents(dashboard, section);
    }

    @Test
    public void addSectionAndWidget_removeWidget_widgetRemoved() {
        DashboardSection section = dashboard.addSection();
        section.add(new DashboardWidget());
        DashboardWidget widget = new DashboardWidget();
        dashboard.add(widget);
        fakeClientCommunication();
        dashboard.remove(widget);
        fakeClientCommunication();
        assertChildComponents(dashboard, section);
    }

    @Test
    public void addSectionAndWidget_removeSection_sectionRemoved() {
        DashboardSection section = dashboard.addSection();
        section.add(new DashboardWidget());
        DashboardWidget widget = new DashboardWidget();
        dashboard.add(widget);
        fakeClientCommunication();
        dashboard.remove(section);
        fakeClientCommunication();
        assertChildComponents(dashboard, widget);
    }

    @Test
    public void addSectionAndWidget_removeAll_widgetAndSectionRemoved() {
        DashboardSection section = dashboard.addSection();
        section.add(new DashboardWidget());
        DashboardWidget widget = new DashboardWidget();
        dashboard.add(widget);
        fakeClientCommunication();
        dashboard.removeAll();
        fakeClientCommunication();
        assertChildComponents(dashboard);
    }

    @Test
    public void addWidgetToSection_widgetIsAdded() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        section.add(widget1, widget2);
        fakeClientCommunication();
        assertSectionWidgets(section, widget1, widget2);
        assertChildComponents(dashboard, section);

    }

    @Test
    public void addNullWidgetToSection_exceptionIsThrown() {
        DashboardSection section = dashboard.addSection();
        Assert.assertThrows(NullPointerException.class,
                () -> section.add((DashboardWidget) null));
        fakeClientCommunication();
        assertChildComponents(dashboard, section);
    }

    @Test
    public void addNullWidgetInArrayToSection_noWidgetIsAdded() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget = new DashboardWidget();
        try {
            section.add(widget, null);
        } catch (NullPointerException e) {
            // Do nothing
        }
        fakeClientCommunication();
        assertSectionWidgets(section);
        assertChildComponents(dashboard, section);
    }

    @Test
    public void addWidgetAtIndexToSection_widgetIsCorrectlyAdded() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        DashboardWidget widget3 = new DashboardWidget();
        section.add(widget1, widget2);
        fakeClientCommunication();
        section.addWidgetAtIndex(1, widget3);
        fakeClientCommunication();
        assertSectionWidgets(section, widget1, widget3, widget2);
        assertChildComponents(dashboard, section);
    }

    @Test
    public void addWidgetAtInvalidIndexToSection_exceptionIsThrown() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        section.add(widget1);
        fakeClientCommunication();
        Assert.assertThrows(IllegalArgumentException.class,
                () -> section.addWidgetAtIndex(2, widget2));
        fakeClientCommunication();
        assertSectionWidgets(section, widget1);
        assertChildComponents(dashboard, section);
    }

    @Test
    public void addWidgetAtNegativeIndexToSection_exceptionIsThrown() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget = new DashboardWidget();
        Assert.assertThrows(IllegalArgumentException.class,
                () -> section.addWidgetAtIndex(-1, widget));
        fakeClientCommunication();
        assertSectionWidgets(section);
        assertChildComponents(dashboard, section);
    }

    @Test
    public void addNullWidgetAtIndexToSection_exceptionIsThrown() {
        DashboardSection section = dashboard.addSection();
        fakeClientCommunication();
        Assert.assertThrows(NullPointerException.class,
                () -> section.addWidgetAtIndex(0, null));
        assertChildComponents(dashboard, section);
    }

    @Test
    public void removeWidgetFromSection_widgetIsRemoved() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        section.add(widget1, widget2);
        fakeClientCommunication();
        section.remove(widget1);
        fakeClientCommunication();
        assertSectionWidgets(section, widget2);
        assertChildComponents(dashboard, section);
    }

    @Test
    public void removeNullWidgetFromSection_exceptionIsThrown() {
        DashboardSection section = dashboard.addSection();
        fakeClientCommunication();
        Assert.assertThrows(NullPointerException.class,
                () -> section.remove((DashboardWidget) null));
        assertChildComponents(dashboard, section);
    }

    @Test
    public void removeAllWidgetsFromSection_widgetsAreRemoved() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        section.add(widget1, widget2);
        fakeClientCommunication();
        section.removeAll();
        fakeClientCommunication();
        assertSectionWidgets(section);
        assertChildComponents(dashboard, section);
    }

    @Test
    public void removeWidgetInSectionFromParent_widgetIsRemoved() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget1 = new DashboardWidget();
        section.add(widget1);
        fakeClientCommunication();
        widget1.removeFromParent();
        fakeClientCommunication();
        assertSectionWidgets(section);
        assertChildComponents(dashboard, section);
    }

    @Test
    public void addMultipleWidgetsToSection_removeOneFromParent_widgetIsRemoved() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        section.add(widget1, widget2);
        fakeClientCommunication();
        widget1.removeFromParent();
        fakeClientCommunication();
        assertSectionWidgets(section, widget2);
        assertChildComponents(dashboard, section);
    }

    @Test
    public void addWidgetsSeparatelyToSection_removeOneFromParent_widgetIsRemoved() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        section.add(widget1);
        section.add(widget2);
        fakeClientCommunication();
        widget1.removeFromParent();
        fakeClientCommunication();
        assertSectionWidgets(section, widget2);
        assertChildComponents(dashboard, section);
    }

    @Test
    public void addWidgetFromLayoutToSection_widgetIsMoved() {
        DashboardSection section = dashboard.addSection();
        Div parent = new Div();
        ui.add(parent);
        DashboardWidget widget = new DashboardWidget();
        parent.add(widget);
        fakeClientCommunication();
        section.add(widget);
        fakeClientCommunication();
        Assert.assertTrue(parent.getChildren().noneMatch(widget::equals));
        assertSectionWidgets(section, widget);
        assertChildComponents(dashboard, section);
    }

    @Test
    public void addWidgetFromSectionToLayout_widgetIsMoved() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget = new DashboardWidget();
        section.add(widget);
        fakeClientCommunication();
        Div parent = new Div();
        ui.add(parent);
        parent.add(widget);
        fakeClientCommunication();
        assertSectionWidgets(section);
        Assert.assertTrue(parent.getChildren().anyMatch(widget::equals));
        assertChildComponents(dashboard, section);
    }

    @Test
    public void addWidgetToAnotherSection_widgetIsMoved() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget = new DashboardWidget();
        section.add(widget);
        fakeClientCommunication();
        DashboardSection newSection = dashboard.addSection();
        newSection.add(widget);
        fakeClientCommunication();
        assertSectionWidgets(section);
        assertSectionWidgets(newSection, widget);
        assertChildComponents(dashboard, section, newSection);
    }

    @Test
    public void setMaximumColumnCount_valueIsCorrectlySet() {
        String propertyName = "--vaadin-dashboard-col-max-count";
        int valueToSet = 5;
        Assert.assertNull(dashboard.getStyle().get(propertyName));
        dashboard.setMaximumColumnCount(valueToSet);
        Assert.assertEquals(String.valueOf(valueToSet),
                dashboard.getStyle().get(propertyName));
        dashboard.setMaximumColumnCount(null);
        Assert.assertNull(dashboard.getStyle().get(propertyName));
    }

    @Test
    public void setMaximumColumnCountNull_propertyIsRemoved() {
        dashboard.setMaximumColumnCount(5);
        dashboard.setMaximumColumnCount(null);
        Assert.assertNull(
                dashboard.getStyle().get("--vaadin-dashboard-col-max-count"));
    }

    @Test
    public void defaultMaximumColumnCountValueIsCorrectlyRetrieved() {
        Assert.assertNull(dashboard.getMaximumColumnCount());
    }

    @Test
    public void setMaximumColumnCount_valueIsCorrectlyRetrieved() {
        Integer valueToSet = 5;
        dashboard.setMaximumColumnCount(valueToSet);
        Assert.assertEquals(valueToSet, dashboard.getMaximumColumnCount());
    }

    @Test
    public void setMaximumColumnCountNull_valueIsCorrectlyRetrieved() {
        dashboard.setMaximumColumnCount(5);
        dashboard.setMaximumColumnCount(null);
        Assert.assertNull(dashboard.getMaximumColumnCount());
    }

    @Test
    public void setWidgetsWithDifferentColspans_itemsAreGeneratedWithCorrectColspans() {
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        widget2.setColspan(2);
        dashboard.add(widget1, widget2);
        fakeClientCommunication();
        assertChildComponents(dashboard, widget1, widget2);
    }

    @Test
    public void setColspanOnExistingWidget_itemsAreUpdatedWithCorrectColspans() {
        DashboardWidget widget = new DashboardWidget();
        dashboard.add(widget);
        fakeClientCommunication();
        widget.setColspan(2);
        fakeClientCommunication();
        assertChildComponents(dashboard, widget);
    }

    @Test
    public void setMaximumColumnWidth_valueIsCorrectlySet() {
        String propertyName = "--vaadin-dashboard-col-max-width";
        String valueToSet = "50px";
        Assert.assertNull(dashboard.getStyle().get(propertyName));
        dashboard.setMaximumColumnWidth(valueToSet);
        Assert.assertEquals(valueToSet, dashboard.getStyle().get(propertyName));
        dashboard.setMaximumColumnWidth(null);
        Assert.assertNull(dashboard.getStyle().get(propertyName));
    }

    @Test
    public void setMaximumColumnWidthNull_propertyIsRemoved() {
        dashboard.setMaximumColumnWidth("50px");
        dashboard.setMaximumColumnWidth(null);
        Assert.assertNull(
                dashboard.getStyle().get("--vaadin-dashboard-col-max-width"));
    }

    @Test
    public void defaultMaximumColumnWidthValueIsCorrectlyRetrieved() {
        Assert.assertNull(dashboard.getMaximumColumnWidth());
    }

    @Test
    public void setMaximumColumnWidth_valueIsCorrectlyRetrieved() {
        String valueToSet = "50px";
        dashboard.setMaximumColumnWidth(valueToSet);
        Assert.assertEquals(valueToSet, dashboard.getMaximumColumnWidth());
    }

    @Test
    public void setMaximumColumnWidthNull_valueIsCorrectlyRetrieved() {
        dashboard.setMaximumColumnWidth("50px");
        dashboard.setMaximumColumnWidth(null);
        Assert.assertNull(dashboard.getMaximumColumnWidth());
    }

    @Test
    public void setMinimumColumnWidth_valueIsCorrectlySet() {
        String propertyName = "--vaadin-dashboard-col-min-width";
        String valueToSet = "50px";
        Assert.assertNull(dashboard.getStyle().get(propertyName));
        dashboard.setMinimumColumnWidth(valueToSet);
        Assert.assertEquals(valueToSet, dashboard.getStyle().get(propertyName));
        dashboard.setMinimumColumnWidth(null);
        Assert.assertNull(dashboard.getStyle().get(propertyName));
    }

    @Test
    public void setMinimumColumnWidthNull_propertyIsRemoved() {
        dashboard.setMinimumColumnWidth("50px");
        dashboard.setMinimumColumnWidth(null);
        Assert.assertNull(
                dashboard.getStyle().get("--vaadin-dashboard-col-min-width"));
    }

    @Test
    public void defaultMinimumColumnWidthValueIsCorrectlyRetrieved() {
        Assert.assertNull(dashboard.getMinimumColumnWidth());
    }

    @Test
    public void setMinimumColumnWidth_valueIsCorrectlyRetrieved() {
        String valueToSet = "50px";
        dashboard.setMinimumColumnWidth(valueToSet);
        Assert.assertEquals(valueToSet, dashboard.getMinimumColumnWidth());
    }

    @Test
    public void setMinimumColumnWidthNull_valueIsCorrectlyRetrieved() {
        dashboard.setMinimumColumnWidth("50px");
        dashboard.setMinimumColumnWidth(null);
        Assert.assertNull(dashboard.getMinimumColumnWidth());
    }

    @Test
    public void setGap_valueIsCorrectlySet() {
        String propertyName = "--vaadin-dashboard-gap";
        String valueToSet = "10px";
        Assert.assertNull(dashboard.getStyle().get(propertyName));
        dashboard.setGap(valueToSet);
        Assert.assertEquals(valueToSet, dashboard.getStyle().get(propertyName));
        dashboard.setGap(null);
        Assert.assertNull(dashboard.getStyle().get(propertyName));
    }

    @Test
    public void setGapNull_propertyIsRemoved() {
        dashboard.setGap("10px");
        dashboard.setGap(null);
        Assert.assertNull(dashboard.getStyle().get("--vaadin-dashboard-gap"));
    }

    @Test
    public void defaultGapValueIsCorrectlyRetrieved() {
        Assert.assertNull(dashboard.getGap());
    }

    @Test
    public void setGap_valueIsCorrectlyRetrieved() {
        String valueToSet = "10px";
        dashboard.setGap(valueToSet);
        Assert.assertEquals(valueToSet, dashboard.getGap());
    }

    @Test
    public void setGapNull_valueIsCorrectlyRetrieved() {
        dashboard.setGap("10px");
        dashboard.setGap(null);
        Assert.assertNull(dashboard.getGap());
    }

    private void fakeClientCommunication() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }

    private static void assertChildComponents(Dashboard dashboard,
            Component... expectedChildren) {
        List<DashboardWidget> expectedWidgets = getExpectedWidgets(
                expectedChildren);
        Assert.assertEquals(expectedWidgets, dashboard.getWidgets());
        Assert.assertEquals(Arrays.asList(expectedChildren),
                dashboard.getChildren().toList());
    }

    private static List<DashboardWidget> getExpectedWidgets(
            Component... expectedChildren) {
        List<DashboardWidget> expectedWidgets = new ArrayList<>();
        for (Component child : expectedChildren) {
            if (child instanceof DashboardSection section) {
                expectedWidgets.addAll(section.getWidgets());
            } else if (child instanceof DashboardWidget widget) {
                expectedWidgets.add(widget);
            } else {
                throw new IllegalArgumentException(
                        "A dashboard can only contain widgets or sections.");
            }
        }
        return expectedWidgets;
    }

    private static void assertSectionWidgets(DashboardSection section,
            DashboardWidget... expectedWidgets) {
        Assert.assertEquals(Arrays.asList(expectedWidgets),
                section.getWidgets());
    }
}
