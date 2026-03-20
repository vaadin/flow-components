/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.shared.HasThemeVariant;

class DashboardTest extends DashboardTestBase {
    private Dashboard dashboard;

    @BeforeEach
    @Override
    void setup() {
        super.setup();
        dashboard = getNewDashboard();
        ui.add(dashboard);
        ui.fakeClientCommunication();
    }

    @Test
    void addWidgetInArray_widgetIsAdded() {
        var widget1 = getNewWidget();
        var widget2 = getNewWidget();
        dashboard.add(widget1, widget2);
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, widget1, widget2);
    }

    @Test
    void addWidgetInCollection_widgetIsAdded() {
        var widget1 = getNewWidget();
        var widget2 = getNewWidget();
        dashboard.add(List.of(widget1, widget2));
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, widget1, widget2);
    }

    @Test
    void addNullWidget_exceptionIsThrown() {
        Assertions.assertThrows(NullPointerException.class,
                () -> dashboard.add((DashboardWidget) null));
    }

    @Test
    void addNullCollection_exceptionIsThrown() {
        Assertions.assertThrows(NullPointerException.class,
                () -> dashboard.add((Collection<DashboardWidget>) null));
    }

    @Test
    void addNullWidgetInArray_noWidgetIsAdded() {
        DashboardWidget widget = getNewWidget();
        try {
            dashboard.add(widget, null);
        } catch (NullPointerException e) {
            // Do nothing
        }
        ui.fakeClientCommunication();
        assertChildComponents(dashboard);
    }

    @Test
    void addNullWidgetInCollection_noWidgetIsAdded() {
        var widgets = new ArrayList<DashboardWidget>();
        widgets.add(getNewWidget());
        widgets.add(null);
        try {
            dashboard.add(widgets);
        } catch (NullPointerException e) {
            // Do nothing
        }
        ui.fakeClientCommunication();
        assertChildComponents(dashboard);
    }

    @Test
    void addWidgetAtIndex_widgetIsCorrectlyAdded() {
        DashboardWidget widget1 = getNewWidget();
        DashboardWidget widget2 = getNewWidget();
        DashboardWidget widget3 = getNewWidget();
        dashboard.add(widget1, widget2);
        ui.fakeClientCommunication();
        dashboard.addWidgetAtIndex(1, widget3);
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, widget1, widget3, widget2);
    }

    @Test
    void addWidgetAtInvalidIndex_exceptionIsThrown() {
        DashboardWidget widget1 = getNewWidget();
        DashboardWidget widget2 = getNewWidget();
        dashboard.add(widget1);
        ui.fakeClientCommunication();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> dashboard.addWidgetAtIndex(2, widget2));
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, widget1);
    }

    @Test
    void addWidgetAtNegativeIndex_exceptionIsThrown() {
        DashboardWidget widget = getNewWidget();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> dashboard.addWidgetAtIndex(-1, widget));
        ui.fakeClientCommunication();
        assertChildComponents(dashboard);
    }

    @Test
    void addNullWidgetAtIndex_exceptionIsThrown() {
        Assertions.assertThrows(NullPointerException.class,
                () -> dashboard.addWidgetAtIndex(0, null));
    }

    @Test
    void addWidgetAfter_rootLevelWidget_widgetIsAddedAfter() {
        DashboardWidget widget1 = getNewWidget();
        DashboardWidget widget2 = getNewWidget();
        DashboardWidget widget3 = getNewWidget();
        dashboard.add(widget1, widget2);
        ui.fakeClientCommunication();
        dashboard.addWidgetAfter(widget1, widget3);
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, widget1, widget3, widget2);
    }

    @Test
    void addWidgetAfter_lastRootLevelWidget_widgetIsAddedAtEnd() {
        DashboardWidget widget1 = getNewWidget();
        DashboardWidget widget2 = getNewWidget();
        DashboardWidget widget3 = getNewWidget();
        dashboard.add(widget1, widget2);
        ui.fakeClientCommunication();
        dashboard.addWidgetAfter(widget2, widget3);
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, widget1, widget2, widget3);
    }

    @Test
    void addWidgetAfter_sectionWidget_widgetIsAddedInSameSection() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget1 = getNewWidget();
        DashboardWidget widget2 = getNewWidget();
        DashboardWidget widget3 = getNewWidget();
        section.add(widget1, widget2);
        ui.fakeClientCommunication();
        dashboard.addWidgetAfter(widget1, widget3);
        ui.fakeClientCommunication();
        assertSectionWidgets(section, widget1, widget3, widget2);
        assertChildComponents(dashboard, section);
    }

    @Test
    void addWidgetAfter_lastSectionWidget_widgetIsAddedAtEndOfSection() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget1 = getNewWidget();
        DashboardWidget widget2 = getNewWidget();
        DashboardWidget widget3 = getNewWidget();
        section.add(widget1, widget2);
        ui.fakeClientCommunication();
        dashboard.addWidgetAfter(widget2, widget3);
        ui.fakeClientCommunication();
        assertSectionWidgets(section, widget1, widget2, widget3);
        assertChildComponents(dashboard, section);
    }

    @Test
    void addWidgetAfter_nullReferenceWidget_exceptionIsThrown() {
        DashboardWidget widget = getNewWidget();
        Assertions.assertThrows(NullPointerException.class,
                () -> dashboard.addWidgetAfter(null, widget));
    }

    @Test
    void addWidgetAfter_nullNewWidget_exceptionIsThrown() {
        DashboardWidget widget = getNewWidget();
        dashboard.add(widget);
        ui.fakeClientCommunication();
        Assertions.assertThrows(NullPointerException.class,
                () -> dashboard.addWidgetAfter(widget, null));
    }

    @Test
    void addWidgetAfter_referenceWidgetNotFound_exceptionIsThrown() {
        DashboardWidget widget1 = getNewWidget();
        DashboardWidget widget2 = getNewWidget();
        dashboard.add(widget1);
        ui.fakeClientCommunication();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> dashboard.addWidgetAfter(widget2, getNewWidget()));
    }

    @Test
    void addWidgetAfter_widgetWithExistingParent_widgetIsMovedAfterReference() {
        DashboardWidget widget1 = getNewWidget();
        DashboardWidget widget2 = getNewWidget();
        DashboardWidget widget3 = getNewWidget();
        dashboard.add(widget1, widget2, widget3);
        ui.fakeClientCommunication();
        dashboard.addWidgetAfter(widget1, widget3);
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, widget1, widget3, widget2);
    }

    @Test
    void addWidgetAfterInSection_nullReferenceWidget_exceptionIsThrown() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget = getNewWidget();
        Assertions.assertThrows(NullPointerException.class,
                () -> section.addWidgetAfter(null, widget));
    }

    @Test
    void addWidgetAfterInSection_referenceWidgetNotFound_exceptionIsThrown() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget1 = getNewWidget();
        DashboardWidget widget2 = getNewWidget();
        section.add(widget1);
        ui.fakeClientCommunication();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> section.addWidgetAfter(widget2, getNewWidget()));
    }

    @Test
    void removeWidgetInArray_widgetIsRemoved() {
        var widget1 = getNewWidget();
        var widget2 = getNewWidget();
        dashboard.add(widget1, widget2);
        ui.fakeClientCommunication();
        dashboard.remove(widget1);
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, widget2);
    }

    @Test
    void removeWidgetInCollection_widgetIsRemoved() {
        var widget1 = getNewWidget();
        var widget2 = getNewWidget();
        dashboard.add(widget1, widget2);
        ui.fakeClientCommunication();
        dashboard.remove(List.of(widget1));
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, widget2);
    }

    @Test
    void removeNullWidget_exceptionIsThrown() {
        Assertions.assertThrows(NullPointerException.class,
                () -> dashboard.remove((DashboardWidget) null));
    }

    @Test
    void removeNullWidgetCollection_exceptionIsThrown() {
        Assertions.assertThrows(NullPointerException.class,
                () -> dashboard.remove((Collection<DashboardWidget>) null));
    }

    @Test
    void removeAllWidgets_widgetsAreRemoved() {
        DashboardWidget widget1 = getNewWidget();
        DashboardWidget widget2 = getNewWidget();
        dashboard.add(widget1, widget2);
        ui.fakeClientCommunication();
        dashboard.removeAll();
        ui.fakeClientCommunication();
        assertChildComponents(dashboard);
    }

    @Test
    void removeWidgetFromParent_widgetIsRemoved() {
        DashboardWidget widget1 = getNewWidget();
        dashboard.add(widget1);
        ui.fakeClientCommunication();
        widget1.removeFromParent();
        ui.fakeClientCommunication();
        assertChildComponents(dashboard);
    }

    @Test
    void addMultipleWidgets_removeOneFromParent_widgetIsRemoved() {
        DashboardWidget widget1 = getNewWidget();
        DashboardWidget widget2 = getNewWidget();
        dashboard.add(widget1, widget2);
        ui.fakeClientCommunication();
        widget1.removeFromParent();
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, widget2);
    }

    @Test
    void addWidgetsSeparately_removeOneFromParent_widgetIsRemoved() {
        DashboardWidget widget1 = getNewWidget();
        DashboardWidget widget2 = getNewWidget();
        dashboard.add(widget1);
        dashboard.add(widget2);
        ui.fakeClientCommunication();
        widget1.removeFromParent();
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, widget2);
    }

    @Test
    void addWidgetFromLayoutToDashboard_widgetIsMoved() {
        Div parent = new Div();
        ui.add(parent);
        DashboardWidget widget = getNewWidget();
        parent.add(widget);
        ui.fakeClientCommunication();
        dashboard.add(widget);
        ui.fakeClientCommunication();
        Assertions.assertTrue(parent.getChildren().noneMatch(widget::equals));
        assertChildComponents(dashboard, widget);
    }

    @Test
    void addWidgetFromDashboardToLayout_widgetIsMoved() {
        DashboardWidget widget = getNewWidget();
        dashboard.add(widget);
        ui.fakeClientCommunication();
        Div parent = new Div();
        ui.add(parent);
        parent.add(widget);
        ui.fakeClientCommunication();
        assertChildComponents(dashboard);
        Assertions.assertTrue(parent.getChildren().anyMatch(widget::equals));
    }

    @Test
    void addWidgetToAnotherDashboard_widgetIsMoved() {
        DashboardWidget widget = getNewWidget();
        dashboard.add(widget);
        ui.fakeClientCommunication();
        Dashboard newDashboard = getNewDashboard();
        ui.add(newDashboard);
        newDashboard.add(widget);
        ui.fakeClientCommunication();
        assertChildComponents(dashboard);
        assertChildComponents(newDashboard, widget);
    }

    @Test
    void addSectionWithoutTitle_sectionIsAdded() {
        DashboardSection section1 = dashboard.addSection();
        DashboardSection section2 = dashboard.addSection();
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, section1, section2);
    }

    @Test
    void addSectionWithNullTitle_sectionIsAdded() {
        DashboardSection section1 = dashboard.addSection((String) null);
        DashboardSection section2 = dashboard.addSection((String) null);
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, section1, section2);
    }

    @Test
    void addSectionWithTitle_sectionIsAdded() {
        DashboardSection section1 = dashboard.addSection("Section 1");
        DashboardSection section2 = dashboard.addSection("Section 2");
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, section1, section2);
    }

    @Test
    void createAndAddSectionWithoutTitle_sectionIsAdded() {
        DashboardSection section1 = new DashboardSection();
        DashboardSection section2 = new DashboardSection();
        dashboard.addSection(section1);
        dashboard.addSection(section2);
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, section1, section2);
    }

    @Test
    void createAndAddSectionWithNullTitle_sectionIsAdded() {
        DashboardSection section1 = new DashboardSection(null);
        DashboardSection section2 = new DashboardSection(null);
        dashboard.addSection(section1);
        dashboard.addSection(section2);
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, section1, section2);
    }

    @Test
    void createAndAddSectionWithTitle_sectionIsAdded() {
        DashboardSection section1 = new DashboardSection("Section 1");
        DashboardSection section2 = new DashboardSection("Section 2");
        dashboard.addSection(section1);
        dashboard.addSection(section2);
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, section1, section2);
    }

    @Test
    void addNullSection_exceptionIsThrown() {
        Assertions.assertThrows(NullPointerException.class,
                () -> dashboard.addSection((DashboardSection) null));
    }

    @Test
    void removeSection_sectionIsRemoved() {
        DashboardSection section1 = dashboard.addSection();
        DashboardSection section2 = dashboard.addSection();
        ui.fakeClientCommunication();
        dashboard.remove(section1);
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, section2);
    }

    @Test
    void removeNullSection_exceptionIsThrown() {
        Assertions.assertThrows(NullPointerException.class,
                () -> dashboard.remove((DashboardSection) null));
    }

    @Test
    void removeAllSections_sectionsAreRemoved() {
        dashboard.addSection();
        dashboard.addSection();
        ui.fakeClientCommunication();
        dashboard.removeAll();
        ui.fakeClientCommunication();
        assertChildComponents(dashboard);
    }

    @Test
    void removeSectionFromParent_sectionIsRemoved() {
        DashboardSection section = dashboard.addSection();
        ui.fakeClientCommunication();
        section.removeFromParent();
        ui.fakeClientCommunication();
        assertChildComponents(dashboard);
    }

    @Test
    void addMultipleSections_removeOneFromParent_sectionIsRemoved() {
        DashboardSection section1 = dashboard.addSection();
        DashboardSection section2 = dashboard.addSection();
        ui.fakeClientCommunication();
        section1.removeFromParent();
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, section2);
    }

    @Test
    void setTitleOnExistingSection_itemsAreUpdatedWithCorrectTitles() {
        DashboardSection section = dashboard.addSection("Section");
        ui.fakeClientCommunication();
        section.setTitle("New title");
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, section);
    }

    @Test
    void addSectionWithWidget_removeWidgetFromDashboard_throwsException() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget = getNewWidget();
        section.add(widget);
        ui.fakeClientCommunication();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> dashboard.remove(widget));
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, section);
    }

    @Test
    void addSection_addWidgetToSection_widgetIsAdded() {
        DashboardSection section = dashboard.addSection();
        ui.fakeClientCommunication();
        DashboardWidget widget = getNewWidget();
        section.add(widget);
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, section);
    }

    @Test
    void addSectionAndWidget_removeWidget_widgetRemoved() {
        DashboardSection section = dashboard.addSection();
        section.add(getNewWidget());
        DashboardWidget widget = getNewWidget();
        dashboard.add(widget);
        ui.fakeClientCommunication();
        dashboard.remove(widget);
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, section);
    }

    @Test
    void addSectionAndWidget_removeSection_sectionRemoved() {
        DashboardSection section = dashboard.addSection();
        section.add(getNewWidget());
        DashboardWidget widget = getNewWidget();
        dashboard.add(widget);
        ui.fakeClientCommunication();
        dashboard.remove(section);
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, widget);
    }

    @Test
    void addSectionAndWidget_removeAll_widgetAndSectionRemoved() {
        DashboardSection section = dashboard.addSection();
        section.add(getNewWidget());
        DashboardWidget widget = getNewWidget();
        dashboard.add(widget);
        ui.fakeClientCommunication();
        dashboard.removeAll();
        ui.fakeClientCommunication();
        assertChildComponents(dashboard);
    }

    @Test
    void addWidgetToSection_widgetIsAdded() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget1 = getNewWidget();
        DashboardWidget widget2 = getNewWidget();
        section.add(widget1, widget2);
        ui.fakeClientCommunication();
        assertSectionWidgets(section, widget1, widget2);
        assertChildComponents(dashboard, section);

    }

    @Test
    void addNullWidgetToSection_exceptionIsThrown() {
        DashboardSection section = dashboard.addSection();
        Assertions.assertThrows(NullPointerException.class,
                () -> section.add((DashboardWidget) null));
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, section);
    }

    @Test
    void addNullWidgetInArrayToSection_noWidgetIsAdded() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget = getNewWidget();
        try {
            section.add(widget, null);
        } catch (NullPointerException e) {
            // Do nothing
        }
        ui.fakeClientCommunication();
        assertSectionWidgets(section);
        assertChildComponents(dashboard, section);
    }

    @Test
    void addWidgetAtIndexToSection_widgetIsCorrectlyAdded() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget1 = getNewWidget();
        DashboardWidget widget2 = getNewWidget();
        DashboardWidget widget3 = getNewWidget();
        section.add(widget1, widget2);
        ui.fakeClientCommunication();
        section.addWidgetAtIndex(1, widget3);
        ui.fakeClientCommunication();
        assertSectionWidgets(section, widget1, widget3, widget2);
        assertChildComponents(dashboard, section);
    }

    @Test
    void addWidgetAtInvalidIndexToSection_exceptionIsThrown() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget1 = getNewWidget();
        DashboardWidget widget2 = getNewWidget();
        section.add(widget1);
        ui.fakeClientCommunication();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> section.addWidgetAtIndex(2, widget2));
        ui.fakeClientCommunication();
        assertSectionWidgets(section, widget1);
        assertChildComponents(dashboard, section);
    }

    @Test
    void addWidgetAtNegativeIndexToSection_exceptionIsThrown() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget = getNewWidget();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> section.addWidgetAtIndex(-1, widget));
        ui.fakeClientCommunication();
        assertSectionWidgets(section);
        assertChildComponents(dashboard, section);
    }

    @Test
    void addNullWidgetAtIndexToSection_exceptionIsThrown() {
        DashboardSection section = dashboard.addSection();
        ui.fakeClientCommunication();
        Assertions.assertThrows(NullPointerException.class,
                () -> section.addWidgetAtIndex(0, null));
        assertChildComponents(dashboard, section);
    }

    @Test
    void removeWidgetFromSection_widgetIsRemoved() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget1 = getNewWidget();
        DashboardWidget widget2 = getNewWidget();
        section.add(widget1, widget2);
        ui.fakeClientCommunication();
        section.remove(widget1);
        ui.fakeClientCommunication();
        assertSectionWidgets(section, widget2);
        assertChildComponents(dashboard, section);
    }

    @Test
    void removeNullWidgetFromSection_exceptionIsThrown() {
        DashboardSection section = dashboard.addSection();
        ui.fakeClientCommunication();
        Assertions.assertThrows(NullPointerException.class,
                () -> section.remove((DashboardWidget) null));
        assertChildComponents(dashboard, section);
    }

    @Test
    void removeAllWidgetsFromSection_widgetsAreRemoved() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget1 = getNewWidget();
        DashboardWidget widget2 = getNewWidget();
        section.add(widget1, widget2);
        ui.fakeClientCommunication();
        section.removeAll();
        ui.fakeClientCommunication();
        assertSectionWidgets(section);
        assertChildComponents(dashboard, section);
    }

    @Test
    void removeWidgetInSectionFromParent_widgetIsRemoved() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget1 = getNewWidget();
        section.add(widget1);
        ui.fakeClientCommunication();
        widget1.removeFromParent();
        ui.fakeClientCommunication();
        assertSectionWidgets(section);
        assertChildComponents(dashboard, section);
    }

    @Test
    void addMultipleWidgetsToSection_removeOneFromParent_widgetIsRemoved() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget1 = getNewWidget();
        DashboardWidget widget2 = getNewWidget();
        section.add(widget1, widget2);
        ui.fakeClientCommunication();
        widget1.removeFromParent();
        ui.fakeClientCommunication();
        assertSectionWidgets(section, widget2);
        assertChildComponents(dashboard, section);
    }

    @Test
    void addWidgetsSeparatelyToSection_removeOneFromParent_widgetIsRemoved() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget1 = getNewWidget();
        DashboardWidget widget2 = getNewWidget();
        section.add(widget1);
        section.add(widget2);
        ui.fakeClientCommunication();
        widget1.removeFromParent();
        ui.fakeClientCommunication();
        assertSectionWidgets(section, widget2);
        assertChildComponents(dashboard, section);
    }

    @Test
    void addWidgetFromLayoutToSection_widgetIsMoved() {
        DashboardSection section = dashboard.addSection();
        Div parent = new Div();
        ui.add(parent);
        DashboardWidget widget = getNewWidget();
        parent.add(widget);
        ui.fakeClientCommunication();
        section.add(widget);
        ui.fakeClientCommunication();
        Assertions.assertTrue(parent.getChildren().noneMatch(widget::equals));
        assertSectionWidgets(section, widget);
        assertChildComponents(dashboard, section);
    }

    @Test
    void addWidgetFromSectionToLayout_widgetIsMoved() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget = getNewWidget();
        section.add(widget);
        ui.fakeClientCommunication();
        Div parent = new Div();
        ui.add(parent);
        parent.add(widget);
        ui.fakeClientCommunication();
        assertSectionWidgets(section);
        Assertions.assertTrue(parent.getChildren().anyMatch(widget::equals));
        assertChildComponents(dashboard, section);
    }

    @Test
    void addWidgetToAnotherSection_widgetIsMoved() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget = getNewWidget();
        section.add(widget);
        ui.fakeClientCommunication();
        DashboardSection newSection = dashboard.addSection();
        newSection.add(widget);
        ui.fakeClientCommunication();
        assertSectionWidgets(section);
        assertSectionWidgets(newSection, widget);
        assertChildComponents(dashboard, section, newSection);
    }

    @Test
    void setMaximumColumnCount_valueIsCorrectlySet() {
        String propertyName = "--vaadin-dashboard-col-max-count";
        int valueToSet = 5;
        Assertions.assertNull(dashboard.getStyle().get(propertyName));
        dashboard.setMaximumColumnCount(valueToSet);
        Assertions.assertEquals(String.valueOf(valueToSet),
                dashboard.getStyle().get(propertyName));
        dashboard.setMaximumColumnCount(null);
        Assertions.assertNull(dashboard.getStyle().get(propertyName));
    }

    @Test
    void setMaximumColumnCountNull_propertyIsRemoved() {
        dashboard.setMaximumColumnCount(5);
        dashboard.setMaximumColumnCount(null);
        Assertions.assertNull(
                dashboard.getStyle().get("--vaadin-dashboard-col-max-count"));
    }

    @Test
    void defaultMaximumColumnCountValueIsCorrectlyRetrieved() {
        Assertions.assertNull(dashboard.getMaximumColumnCount());
    }

    @Test
    void setMaximumColumnCount_valueIsCorrectlyRetrieved() {
        Integer valueToSet = 5;
        dashboard.setMaximumColumnCount(valueToSet);
        Assertions.assertEquals(valueToSet, dashboard.getMaximumColumnCount());
    }

    @Test
    void setMaximumColumnCountNull_valueIsCorrectlyRetrieved() {
        dashboard.setMaximumColumnCount(5);
        dashboard.setMaximumColumnCount(null);
        Assertions.assertNull(dashboard.getMaximumColumnCount());
    }

    @Test
    void setWidgetsWithDifferentColspans_itemsAreGeneratedWithCorrectColspans() {
        DashboardWidget widget1 = getNewWidget();
        DashboardWidget widget2 = getNewWidget();
        widget2.setColspan(2);
        dashboard.add(widget1, widget2);
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, widget1, widget2);
    }

    @Test
    void setColspanOnExistingWidget_itemsAreUpdatedWithCorrectColspans() {
        DashboardWidget widget = getNewWidget();
        dashboard.add(widget);
        ui.fakeClientCommunication();
        widget.setColspan(2);
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, widget);
    }

    @Test
    void setMaximumColumnWidth_valueIsCorrectlySet() {
        String propertyName = "--vaadin-dashboard-col-max-width";
        String valueToSet = "50px";
        Assertions.assertNull(dashboard.getStyle().get(propertyName));
        dashboard.setMaximumColumnWidth(valueToSet);
        Assertions.assertEquals(valueToSet,
                dashboard.getStyle().get(propertyName));
        dashboard.setMaximumColumnWidth(null);
        Assertions.assertNull(dashboard.getStyle().get(propertyName));
    }

    @Test
    void setMaximumColumnWidthNull_propertyIsRemoved() {
        dashboard.setMaximumColumnWidth("50px");
        dashboard.setMaximumColumnWidth(null);
        Assertions.assertNull(
                dashboard.getStyle().get("--vaadin-dashboard-col-max-width"));
    }

    @Test
    void defaultMaximumColumnWidthValueIsCorrectlyRetrieved() {
        Assertions.assertNull(dashboard.getMaximumColumnWidth());
    }

    @Test
    void setMaximumColumnWidth_valueIsCorrectlyRetrieved() {
        String valueToSet = "50px";
        dashboard.setMaximumColumnWidth(valueToSet);
        Assertions.assertEquals(valueToSet, dashboard.getMaximumColumnWidth());
    }

    @Test
    void setMaximumColumnWidthNull_valueIsCorrectlyRetrieved() {
        dashboard.setMaximumColumnWidth("50px");
        dashboard.setMaximumColumnWidth(null);
        Assertions.assertNull(dashboard.getMaximumColumnWidth());
    }

    @Test
    void setMinimumColumnWidth_valueIsCorrectlySet() {
        String propertyName = "--vaadin-dashboard-col-min-width";
        String valueToSet = "50px";
        Assertions.assertNull(dashboard.getStyle().get(propertyName));
        dashboard.setMinimumColumnWidth(valueToSet);
        Assertions.assertEquals(valueToSet,
                dashboard.getStyle().get(propertyName));
        dashboard.setMinimumColumnWidth(null);
        Assertions.assertNull(dashboard.getStyle().get(propertyName));
    }

    @Test
    void setMinimumColumnWidthNull_propertyIsRemoved() {
        dashboard.setMinimumColumnWidth("50px");
        dashboard.setMinimumColumnWidth(null);
        Assertions.assertNull(
                dashboard.getStyle().get("--vaadin-dashboard-col-min-width"));
    }

    @Test
    void defaultMinimumColumnWidthValueIsCorrectlyRetrieved() {
        Assertions.assertNull(dashboard.getMinimumColumnWidth());
    }

    @Test
    void setMinimumColumnWidth_valueIsCorrectlyRetrieved() {
        String valueToSet = "50px";
        dashboard.setMinimumColumnWidth(valueToSet);
        Assertions.assertEquals(valueToSet, dashboard.getMinimumColumnWidth());
    }

    @Test
    void setMinimumColumnWidthNull_valueIsCorrectlyRetrieved() {
        dashboard.setMinimumColumnWidth("50px");
        dashboard.setMinimumColumnWidth(null);
        Assertions.assertNull(dashboard.getMinimumColumnWidth());
    }

    @Test
    void setMinimumRowHeight_valueIsCorrectlySet() {
        String propertyName = "--vaadin-dashboard-row-min-height";
        String valueToSet = "200px";
        Assertions.assertNull(dashboard.getStyle().get(propertyName));
        dashboard.setMinimumRowHeight(valueToSet);
        Assertions.assertEquals(valueToSet,
                dashboard.getStyle().get(propertyName));
        dashboard.setMinimumRowHeight(null);
        Assertions.assertNull(dashboard.getStyle().get(propertyName));
    }

    @Test
    void setMinimumRowHeightNull_propertyIsRemoved() {
        dashboard.setMinimumRowHeight("200px");
        dashboard.setMinimumRowHeight(null);
        Assertions.assertNull(
                dashboard.getStyle().get("--vaadin-dashboard-row-min-height"));
    }

    @Test
    void defaultMinimumRowHeightValueIsCorrectlyRetrieved() {
        Assertions.assertNull(dashboard.getMinimumRowHeight());
    }

    @Test
    void setMinimumRowHeight_valueIsCorrectlyRetrieved() {
        String valueToSet = "200px";
        dashboard.setMinimumRowHeight(valueToSet);
        Assertions.assertEquals(valueToSet, dashboard.getMinimumRowHeight());
    }

    @Test
    void setMinimumRowHeightNull_valueIsCorrectlyRetrieved() {
        dashboard.setMinimumRowHeight("200px");
        dashboard.setMinimumRowHeight(null);
        Assertions.assertNull(dashboard.getMinimumRowHeight());
    }

    @Test
    void setGap_valueIsCorrectlySet() {
        String propertyName = "--vaadin-dashboard-gap";
        String valueToSet = "10px";
        Assertions.assertNull(dashboard.getStyle().get(propertyName));
        dashboard.setGap(valueToSet);
        Assertions.assertEquals(valueToSet,
                dashboard.getStyle().get(propertyName));
        dashboard.setGap(null);
        Assertions.assertNull(dashboard.getStyle().get(propertyName));
    }

    @Test
    void setGapNull_propertyIsRemoved() {
        dashboard.setGap("10px");
        dashboard.setGap(null);
        Assertions
                .assertNull(dashboard.getStyle().get("--vaadin-dashboard-gap"));
    }

    @Test
    void defaultGapValueIsCorrectlyRetrieved() {
        Assertions.assertNull(dashboard.getGap());
    }

    @Test
    void setGap_valueIsCorrectlyRetrieved() {
        String valueToSet = "10px";
        dashboard.setGap(valueToSet);
        Assertions.assertEquals(valueToSet, dashboard.getGap());
    }

    @Test
    void setGapNull_valueIsCorrectlyRetrieved() {
        dashboard.setGap("10px");
        dashboard.setGap(null);
        Assertions.assertNull(dashboard.getGap());
    }

    @Test
    void setPadding_valueIsCorrectlySet() {
        String propertyName = "--vaadin-dashboard-padding";
        String valueToSet = "10px";
        Assertions.assertNull(dashboard.getStyle().get(propertyName));
        dashboard.setPadding(valueToSet);
        Assertions.assertEquals(valueToSet,
                dashboard.getStyle().get(propertyName));
        dashboard.setPadding(null);
        Assertions.assertNull(dashboard.getStyle().get(propertyName));
    }

    @Test
    void setPaddingNull_propertyIsRemoved() {
        dashboard.setPadding("10px");
        dashboard.setPadding(null);
        Assertions.assertNull(
                dashboard.getStyle().get("--vaadin-dashboard-padding"));
    }

    @Test
    void defaultPaddingValueIsCorrectlyRetrieved() {
        Assertions.assertNull(dashboard.getPadding());
    }

    @Test
    void setPadding_valueIsCorrectlyRetrieved() {
        String valueToSet = "10px";
        dashboard.setPadding(valueToSet);
        Assertions.assertEquals(valueToSet, dashboard.getPadding());
    }

    @Test
    void setPaddingNull_valueIsCorrectlyRetrieved() {
        dashboard.setPadding("10px");
        dashboard.setPadding(null);
        Assertions.assertNull(dashboard.getPadding());
    }

    @Test
    void dashboardIsNotEditableByDefault() {
        Assertions.assertFalse(dashboard.isEditable());
    }

    @Test
    void setEditableFalse_valueIsCorrectlyRetrieved() {
        dashboard.setEditable(false);
        Assertions.assertFalse(dashboard.isEditable());
    }

    @Test
    void setEditableTrue_valueIsCorrectlyRetrieved() {
        dashboard.setEditable(false);
        dashboard.setEditable(true);
        Assertions.assertTrue(dashboard.isEditable());
    }

    @Test
    void dashboardIsNotDenseLayoutByDefault() {
        Assertions.assertFalse(dashboard.isDenseLayout());
    }

    @Test
    void setDenseLayoutFalse_valueIsCorrectlyRetrieved() {
        dashboard.setDenseLayout(false);
        Assertions.assertFalse(dashboard.isDenseLayout());
    }

    @Test
    void setDenseLayoutTrue_valueIsCorrectlyRetrieved() {
        dashboard.setDenseLayout(false);
        dashboard.setDenseLayout(true);
        Assertions.assertTrue(dashboard.isDenseLayout());
    }

    @Test
    void addWidget_detachDashboard_widgetIsRetained() {
        DashboardWidget widget = getNewWidget();
        dashboard.add(widget);
        ui.fakeClientCommunication();
        ui.remove(dashboard);
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, widget);
    }

    @Test
    void detachDashboard_addWidget_reattachDashboard_widgetIsAdded() {
        ui.remove(dashboard);
        ui.fakeClientCommunication();
        DashboardWidget widget = getNewWidget();
        dashboard.add(widget);
        ui.fakeClientCommunication();
        assertChildComponents(dashboard, widget);
    }

    @Test
    void dashboardNotEditable_removeWidget_widgetIsNotRemoved() {
        DashboardWidget widgetToRemove = getNewWidget();
        dashboard.add(widgetToRemove);
        ui.fakeClientCommunication();
        int expectedWidgetCount = dashboard.getWidgets().size();
        int expectedNodeId = widgetToRemove.getElement().getNode().getId();
        DashboardTestHelper.fireItemBeforeRemoveEvent(dashboard,
                expectedNodeId);
        Assertions.assertEquals(expectedWidgetCount,
                dashboard.getWidgets().size());
        Set<Integer> actualNodeIds = dashboard.getWidgets().stream()
                .map(widget -> widget.getElement().getNode().getId())
                .collect(Collectors.toSet());
        Assertions.assertTrue(actualNodeIds.contains(expectedNodeId));
    }

    @Test
    void setDashboardEditable_removeWidget_widgetIsRemoved() {
        DashboardWidget widgetToRemove = getNewWidget();
        dashboard.add(widgetToRemove);
        dashboard.setEditable(true);
        ui.fakeClientCommunication();
        int expectedWidgetCount = dashboard.getWidgets().size() - 1;
        int nodeIdToBeRemoved = widgetToRemove.getElement().getNode().getId();
        DashboardTestHelper.fireItemBeforeRemoveEvent(dashboard,
                nodeIdToBeRemoved);
        Assertions.assertEquals(expectedWidgetCount,
                dashboard.getWidgets().size());
        Set<Integer> actualNodeIds = dashboard.getWidgets().stream()
                .map(widget -> widget.getElement().getNode().getId())
                .collect(Collectors.toSet());
        Assertions.assertFalse(actualNodeIds.contains(nodeIdToBeRemoved));
    }

    @Test
    void setDashboardEditable_removeWidget_clientUpdate() {
        DashboardWidget widgetToRemove = getNewWidget();
        dashboard.add(widgetToRemove);
        dashboard.setEditable(true);
        ui.fakeClientCommunication();
        ui.dumpPendingJavaScriptInvocations();

        int nodeIdToBeRemoved = widgetToRemove.getElement().getNode().getId();
        DashboardTestHelper.fireItemBeforeRemoveEvent(dashboard,
                nodeIdToBeRemoved);
        ui.fakeClientCommunication();

        Assertions.assertFalse(ui.dumpPendingJavaScriptInvocations().isEmpty());
    }

    @Test
    void setDashboardEditable_removeWidget_eventCorrectlyFired() {
        dashboard.setEditable(true);
        DashboardWidget widget = getNewWidget();
        dashboard.add(widget);
        ui.fakeClientCommunication();
        int removedWidgetNodeId = widget.getElement().getNode().getId();
        List<Component> expectedItems = dashboard.getChildren()
                .collect(Collectors.toCollection(ArrayList::new));
        expectedItems.remove(widget);
        assertItemRemoveEventCorrectlyFired(removedWidgetNodeId, 1, widget,
                expectedItems);
    }

    @Test
    void setDashboardEditable_removeSection_eventCorrectlyFired() {
        dashboard.setEditable(true);
        DashboardSection section = dashboard.addSection();
        ui.fakeClientCommunication();
        int removedSectionNodeId = section.getElement().getNode().getId();
        List<Component> expectedItems = dashboard.getChildren()
                .collect(Collectors.toCollection(ArrayList::new));
        expectedItems.remove(section);
        assertItemRemoveEventCorrectlyFired(removedSectionNodeId, 1, section,
                expectedItems);
    }

    @Test
    void setDashboardEditable_removeWidgetInSection_eventCorrectlyFired() {
        dashboard.setEditable(true);
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget = getNewWidget();
        section.add(widget);
        ui.fakeClientCommunication();
        int removedWidgetNodeId = widget.getElement().getNode().getId();
        List<Component> expectedItems = dashboard.getChildren()
                .collect(Collectors.toCollection(ArrayList::new));
        expectedItems.remove(widget);
        assertItemRemoveEventCorrectlyFired(removedWidgetNodeId, 1, widget,
                expectedItems);
    }

    @Test
    void dashboardNotEditable_removeWidget_eventNotFired() {
        DashboardWidget widget = getNewWidget();
        dashboard.add(widget);
        ui.fakeClientCommunication();
        int removedWidgetNodeId = widget.getElement().getNode().getId();
        assertItemRemoveEventCorrectlyFired(removedWidgetNodeId, 0, null, null);
    }

    @Test
    void setItemRemoveHandler_widgetNotRemovedAutomatically() {
        dashboard.setEditable(true);
        DashboardWidget widget = getNewWidget();
        dashboard.add(widget);
        ui.fakeClientCommunication();

        dashboard.setItemRemoveHandler(e -> {
        });

        int nodeId = widget.getElement().getNode().getId();
        DashboardTestHelper.fireItemBeforeRemoveEvent(dashboard, nodeId);

        Assertions.assertEquals(1, dashboard.getWidgets().size());
        Assertions.assertTrue(dashboard.getWidgets().contains(widget));
    }

    @Test
    void setItemRemoveHandler_callRemoveItem_widgetRemoved() {
        dashboard.setEditable(true);
        DashboardWidget widget = getNewWidget();
        dashboard.add(widget);
        ui.fakeClientCommunication();

        AtomicReference<DashboardItemRemovedEvent> removedEvent = new AtomicReference<>();
        dashboard.addItemRemovedListener(removedEvent::set);
        dashboard.setItemRemoveHandler(DashboardItemRemoveEvent::removeItem);

        int nodeId = widget.getElement().getNode().getId();
        DashboardTestHelper.fireItemBeforeRemoveEvent(dashboard, nodeId);

        Assertions.assertEquals(0, dashboard.getWidgets().size());
        Assertions.assertNotNull(removedEvent.get());
        Assertions.assertEquals(widget, removedEvent.get().getItem());
    }

    @Test
    void setItemRemoveHandler_callRemoveItem_sectionRemoved() {
        dashboard.setEditable(true);
        DashboardSection section = dashboard.addSection();
        ui.fakeClientCommunication();

        AtomicReference<DashboardItemRemovedEvent> removedEvent = new AtomicReference<>();
        dashboard.addItemRemovedListener(removedEvent::set);
        dashboard.setItemRemoveHandler(DashboardItemRemoveEvent::removeItem);

        int nodeId = section.getElement().getNode().getId();
        DashboardTestHelper.fireItemBeforeRemoveEvent(dashboard, nodeId);

        Assertions.assertEquals(0, dashboard.getChildren().count());
        Assertions.assertNotNull(removedEvent.get());
        Assertions.assertEquals(section, removedEvent.get().getItem());
    }

    @Test
    void setItemRemoveHandler_callRemoveItem_clientUpdate() {
        DashboardWidget widgetToRemove = getNewWidget();
        dashboard.add(widgetToRemove);
        dashboard.setEditable(true);
        ui.fakeClientCommunication();
        ui.dumpPendingJavaScriptInvocations();

        dashboard.setItemRemoveHandler(DashboardItemRemoveEvent::removeItem);

        int nodeIdToBeRemoved = widgetToRemove.getElement().getNode().getId();
        DashboardTestHelper.fireItemBeforeRemoveEvent(dashboard,
                nodeIdToBeRemoved);
        ui.fakeClientCommunication();

        Assertions.assertFalse(ui.dumpPendingJavaScriptInvocations().isEmpty());
    }

    @Test
    void setItemRemoveHandler_widgetAndSectionProvidedInEvent() {
        dashboard.setEditable(true);
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget = getNewWidget();
        section.add(widget);
        ui.fakeClientCommunication();

        AtomicReference<DashboardItemRemoveEvent> capturedEvent = new AtomicReference<>();
        dashboard.setItemRemoveHandler(capturedEvent::set);

        // Removing the widget in the section
        int widgetNodeId = widget.getElement().getNode().getId();
        int sectionNodeId = section.getElement().getNode().getId();
        DashboardTestHelper.fireItemBeforeRemoveEvent(dashboard, widgetNodeId,
                sectionNodeId);

        Assertions.assertNotNull(capturedEvent.get());
        Assertions.assertEquals(widget, capturedEvent.get().getItem());
        Assertions.assertEquals(section,
                capturedEvent.get().getSection().orElse(null));

        // Removing the section
        capturedEvent.set(null);

        DashboardTestHelper.fireItemBeforeRemoveEvent(dashboard, sectionNodeId);

        Assertions.assertNotNull(capturedEvent.get());
        Assertions.assertEquals(section, capturedEvent.get().getItem());
        Assertions.assertTrue(capturedEvent.get().getSection().isEmpty());
    }

    @Test
    void setItemRemoveHandler_removeHandler_defaultBehaviorRestored() {
        dashboard.setEditable(true);
        DashboardWidget widget = getNewWidget();
        dashboard.add(widget);
        ui.fakeClientCommunication();

        dashboard.setItemRemoveHandler(event -> {
        });
        dashboard.setItemRemoveHandler(null);

        int nodeId = widget.getElement().getNode().getId();
        DashboardTestHelper.fireItemBeforeRemoveEvent(dashboard, nodeId);

        Assertions.assertEquals(0, dashboard.getWidgets().size());
    }

    @Test
    void getItemRemoveHandler_returnsSetHandler() {
        Assertions.assertNull(dashboard.getItemRemoveHandler());

        DashboardItemRemoveHandler handler = event -> {
        };
        dashboard.setItemRemoveHandler(handler);
        Assertions.assertEquals(handler, dashboard.getItemRemoveHandler());

        dashboard.setItemRemoveHandler(null);
        Assertions.assertNull(dashboard.getItemRemoveHandler());
    }

    @Test
    void setDashboardVisibility_exceptionIsThrown() {
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> dashboard.setVisible(false));
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> dashboard.setVisible(true));
    }

    @Test
    void setDashboardSectionVisibility_exceptionIsThrown() {
        DashboardSection section = dashboard.addSection();
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> section.setVisible(false));
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> section.setVisible(true));
    }

    @Test
    void getDashboardVisibility_returnsTrue() {
        Assertions.assertTrue(dashboard.isVisible());
    }

    @Test
    void getSectionVisibility_returnsTrue() {
        DashboardSection section = dashboard.addSection();
        Assertions.assertTrue(section.isVisible());
    }

    @Test
    void changeWidgetSelectedState_eventCorrectlyFired() {
        DashboardWidget widget = getNewWidget();
        dashboard.add(widget);
        assertItemSelectedChangedEventCorrectlyFired(widget, true);
        assertItemSelectedChangedEventCorrectlyFired(widget, false);
    }

    @Test
    void changeSectionSelectedState_eventCorrectlyFired() {
        DashboardSection section = dashboard.addSection();
        assertItemSelectedChangedEventCorrectlyFired(section, true);
        assertItemSelectedChangedEventCorrectlyFired(section, false);
    }

    @Test
    void changeWidgetInSectionSelectedState_eventCorrectlyFired() {
        DashboardSection section = dashboard.addSection();
        DashboardWidget widget = getNewWidget();
        section.add(widget);
        assertItemSelectedChangedEventCorrectlyFired(widget, true);
        assertItemSelectedChangedEventCorrectlyFired(widget, false);
    }

    @Test
    void setRootHeadingLevel_elementPropertyIsUpdated() {
        var rootHeadingLevel = 1;
        dashboard.setRootHeadingLevel(rootHeadingLevel);
        Assertions.assertEquals(rootHeadingLevel,
                dashboard.getElement().getProperty("rootHeadingLevel", -1));
        rootHeadingLevel = 7;
        dashboard.setRootHeadingLevel(rootHeadingLevel);
        Assertions.assertEquals(rootHeadingLevel,
                dashboard.getElement().getProperty("rootHeadingLevel", -1));
    }

    @Test
    void setTitleHeadingLevelNull_elementPropertyIsRemoved() {
        dashboard.setRootHeadingLevel(1);
        dashboard.setRootHeadingLevel(null);
        Assertions.assertFalse(
                dashboard.getElement().hasProperty("rootHeadingLevel"));
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions.assertTrue(
                HasThemeVariant.class.isAssignableFrom(Dashboard.class));
    }

    private void assertItemSelectedChangedEventCorrectlyFired(Component item,
            boolean selected) {
        AtomicInteger listenerInvokedCount = new AtomicInteger(0);
        AtomicReference<Component> eventItem = new AtomicReference<>();
        AtomicReference<Boolean> eventIsSelected = new AtomicReference<>();
        dashboard.addItemSelectedChangedListener(e -> {
            listenerInvokedCount.incrementAndGet();
            eventItem.set(e.getItem());
            eventIsSelected.set(e.isSelected());
            e.unregisterListener();
        });
        DashboardTestHelper.fireItemSelectedChangedEvent(dashboard,
                item.getElement().getNode().getId(), selected);
        Assertions.assertEquals(1, listenerInvokedCount.get());
        Assertions.assertEquals(item, eventItem.get());
        Assertions.assertEquals(selected, eventIsSelected.get());
    }

    private void assertItemRemoveEventCorrectlyFired(int nodeIdToRemove,
            int expectedListenerInvokedCount, Component expectedRemovedItem,
            List<Component> expectedItems) {
        AtomicInteger listenerInvokedCount = new AtomicInteger(0);
        AtomicReference<Component> eventRemovedItem = new AtomicReference<>();
        AtomicReference<List<Component>> eventItems = new AtomicReference<>();
        dashboard.addItemRemovedListener(e -> {
            listenerInvokedCount.incrementAndGet();
            eventRemovedItem.set(e.getItem());
            eventItems.set(e.getItems());
            e.unregisterListener();
        });
        DashboardTestHelper.fireItemBeforeRemoveEvent(dashboard,
                nodeIdToRemove);
        Assertions.assertEquals(expectedListenerInvokedCount,
                listenerInvokedCount.get());
        if (expectedListenerInvokedCount > 0) {
            Assertions.assertEquals(expectedRemovedItem,
                    eventRemovedItem.get());
            Assertions.assertEquals(expectedItems, eventItems.get());
        }
    }
}
