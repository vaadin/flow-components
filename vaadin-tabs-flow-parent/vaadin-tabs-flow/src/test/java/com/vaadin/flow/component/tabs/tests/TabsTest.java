/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.tabs.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;

/**
 * @author Vaadin Ltd.
 */
class TabsTest {

    @Test
    void createTabsInDefaultState() {
        Tabs tabs = new Tabs();

        Assertions.assertEquals(0, tabs.getTabCount(),
                "Initial tab count is invalid");
        Assertions.assertEquals(Tabs.Orientation.HORIZONTAL,
                tabs.getOrientation(), "Initial orientation is invalid");
        Assertions.assertEquals(-1, tabs.getSelectedIndex(),
                "Initial selected index is invalid");
        Assertions.assertNull(tabs.getSelectedTab(),
                "Initial child count is invalid");
    }

    @Test
    void createTabsWithChildren() {
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tab tab3 = new Tab("Tab three");
        Tabs tabs = new Tabs(tab1, tab2, tab3);

        Assertions.assertEquals(3, tabs.getTabCount(),
                "Initial tab count is invalid");
        Assertions.assertEquals(Tabs.Orientation.HORIZONTAL,
                tabs.getOrientation(), "Initial orientation is invalid");
        Assertions.assertEquals(tab1, tabs.getSelectedTab(),
                "Initial selected tab is invalid");
        Assertions.assertEquals(0, tabs.getSelectedIndex(),
                "Initial selected index is invalid");
    }

    @Test
    void setOrientation() {
        Tabs tabs = new Tabs();

        tabs.setOrientation(Tabs.Orientation.VERTICAL);

        Assertions.assertEquals(Tabs.Orientation.VERTICAL,
                tabs.getOrientation(), "Orientation is invalid");
    }

    @Test
    void selectTabByReference() {
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tab tab3 = new Tab("Tab three");
        Tabs tabs = new Tabs(tab1, tab2, tab3);

        tabs.setSelectedTab(tab2);

        Assertions.assertEquals(tab2, tabs.getSelectedTab(),
                "Selected tab is invalid");
        Assertions.assertEquals(1, tabs.getSelectedIndex(),
                "Selected index is invalid");
    }

    @Test
    void selectTabByIndex() {
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tab tab3 = new Tab("Tab three");
        Tabs tabs = new Tabs(tab1, tab2, tab3);

        tabs.setSelectedIndex(2);

        Assertions.assertEquals(tab3, tabs.getSelectedTab(),
                "Selected tab is invalid");
        Assertions.assertEquals(2, tabs.getSelectedIndex(),
                "Selected index is invalid");
    }

    @Test
    void selectInvalidIndex_previousIndexIsReverted() {
        Tab tab = new Tab("Tab");
        Tabs tabs = new Tabs(tab);

        // Select index out of range
        tabs.setSelectedIndex(10);
        Assertions.assertEquals(0, tabs.getSelectedIndex());

        // Deselect the active tab
        tabs.setSelectedIndex(-1);
        // Select index out of range
        tabs.setSelectedIndex(10);
        Assertions.assertEquals(-1, tabs.getSelectedIndex());
    }

    @Test
    void selectInvalidIndex_warningIsShown() {
        Tab tab = new Tab("Tab");
        Tabs tabs = new Tabs(tab);

        Logger mockedLogger = Mockito.mock(Logger.class);
        try (MockedStatic<LoggerFactory> context = Mockito
                .mockStatic(LoggerFactory.class)) {
            context.when(() -> LoggerFactory.getLogger(Tabs.class))
                    .thenReturn(mockedLogger);

            // Select index out of range
            tabs.setSelectedIndex(10);

            Mockito.verify(mockedLogger, Mockito.times(1)).warn(
                    "The selected index is out of range: 10. Reverting to the previous index: 0.");
        }
    }

    @Test
    void shouldThrowWhenTabToSelectIsNotChild() {
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tab tab3 = new Tab("orphan");
        Tabs tabs = new Tabs(tab1, tab2);

        var exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> tabs.setSelectedTab(tab3));
        Assertions.assertEquals("Tab to select must be a child: Tab{orphan}",
                exception.getMessage());
    }

    @Test
    void setFlexGrowForEnclosedTabs() {
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tab tab3 = new Tab("Tab three");
        Tabs tabs = new Tabs(tab1, tab2, tab3);

        tabs.setFlexGrowForEnclosedTabs(1.5);

        Assertions.assertEquals(1.5, tab1.getFlexGrow(), 0.0,
                "flexGrow of tab1 is invalid");
        Assertions.assertEquals(1.5, tab2.getFlexGrow(), 0.0,
                "flexGrow of tab2 is invalid");
        Assertions.assertEquals(1.5, tab3.getFlexGrow(), 0.0,
                "flexGrow of tab3 is invalid");
    }

    @Test
    void shouldThrowOnNegativeFlexGrow() {
        Tabs tabs = new Tabs();

        var exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> tabs.setFlexGrowForEnclosedTabs(-1));
        Assertions.assertEquals("Flex grow property must not be negative",
                exception.getMessage());
    }

    @Test
    void selectTab_tabIsSelected() {
        Tabs tabs = new Tabs();
        Tab tab1 = new Tab("foo");
        Tab tab2 = new Tab("foo");
        tabs.add(tab1, tab2);

        tabs.setSelectedTab(tab2);

        Assertions.assertFalse(tab1.isSelected());
        Assertions.assertTrue(tab2.isSelected());
    }

    @Test
    void tabsAutoselectConstructor() {
        Tabs tabs1 = new Tabs(true);
        tabs1.add(new Tab("Tab"));
        Assertions.assertEquals(0, tabs1.getSelectedIndex());

        Tabs tabs2 = new Tabs(false);
        tabs2.add(new Tab("Tab"));
        Assertions.assertEquals(-1, tabs2.getSelectedIndex());
    }

    @Test
    void tabsWithoutAutomaticSelection() {
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tabs tabs2 = new Tabs(false, tab1, tab2);

        Assertions.assertNull(tabs2.getSelectedTab());
        Assertions.assertEquals(-1, tabs2.getSelectedIndex());
    }

    @Test
    void removeTabInTabsWithoutAutomaticSelection() {
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tab tab3 = new Tab("Tab three");
        Tabs tabs = new Tabs(false, tab1, tab2, tab3);

        tabs.setSelectedTab(tab2);
        tabs.remove(tab1);

        Assertions.assertEquals(tabs.getSelectedTab(), tab2,
                "should not change selected tab");

        tabs.remove(tab2);
        Assertions.assertNull(tabs.getSelectedTab(),
                "should not select other tab if current tab removed");
    }

    @Test
    void removeTabInTabsWithoutSelection() {
        Tab tab1 = new Tab("Tab one");
        Tabs tabs = new Tabs(tab1);

        tabs.setSelectedTab(null);
        tabs.remove(tab1);

        Assertions.assertNull(tabs.getSelectedTab(),
                "should not change selected tab");
    }

    @Test
    void addTabsAsComponentArray_noClassCastExceptionIsThrown() {
        Tabs tabs = new Tabs();
        Component[] tabsArray = { new Tab(), new Tab() };

        // we test that the following call does not fail with ClassCastException
        tabs.add(tabsArray);

        // assertion here just to make sure tabs were really set
        Assertions.assertNotNull(tabs.getSelectedTab());
    }

    @Test
    void addTabsToDisabledContainer_reEnable_tabsShouldBeEnabled() {
        var container = new Div();
        var tabs = new Tabs();
        container.add(tabs);
        var tab1 = new Tab("Tab one");
        var tab2 = new Tab("Tab two");

        container.setEnabled(false);
        tabs.add(tab1, tab2);
        container.setEnabled(true);

        Assertions.assertTrue(tab1.isEnabled());
        Assertions.assertTrue(tab2.isEnabled());
    }

    @Test
    void addTabsToDisabledContainer_reEnable_shouldHaveSelectedTab() {
        var container = new Div();
        var tabs = new Tabs();
        container.add(tabs);
        var tab1 = new Tab("Tab one");
        var tab2 = new Tab("Tab two");

        container.setEnabled(false);
        tabs.add(tab1, tab2);
        container.setEnabled(true);

        Assertions.assertEquals(tab1, tabs.getSelectedTab());
    }

    @Test
    void addDisabledTab_shouldNotHaveSelectedTab() {
        var tabs = new Tabs();
        var tab1 = new Tab("Tab one");

        tab1.setEnabled(false);
        tabs.add(tab1);
        tabs.setSelectedIndex(0);

        Assertions.assertEquals(null, tabs.getSelectedTab());
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions
                .assertTrue(HasThemeVariant.class.isAssignableFrom(Tabs.class));
    }
}
