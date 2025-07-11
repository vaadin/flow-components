/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;

/**
 * @author Vaadin Ltd.
 */
public class TabsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void createTabsInDefaultState() {
        Tabs tabs = new Tabs();

        Assert.assertEquals("Initial tab count is invalid", 0,
                tabs.getTabCount());
        Assert.assertEquals("Initial orientation is invalid",
                Tabs.Orientation.HORIZONTAL, tabs.getOrientation());
        Assert.assertEquals("Initial selected index is invalid", -1,
                tabs.getSelectedIndex());
        Assert.assertNull("Initial child count is invalid",
                tabs.getSelectedTab());
    }

    @Test
    public void createTabsWithChildren() {
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tab tab3 = new Tab("Tab three");
        Tabs tabs = new Tabs(tab1, tab2, tab3);

        Assert.assertEquals("Initial tab count is invalid", 3,
                tabs.getTabCount());
        Assert.assertEquals("Initial orientation is invalid",
                Tabs.Orientation.HORIZONTAL, tabs.getOrientation());
        Assert.assertEquals("Initial selected tab is invalid", tab1,
                tabs.getSelectedTab());
        Assert.assertEquals("Initial selected index is invalid", 0,
                tabs.getSelectedIndex());
    }

    @Test
    public void setOrientation() {
        Tabs tabs = new Tabs();

        tabs.setOrientation(Tabs.Orientation.VERTICAL);

        Assert.assertEquals("Orientation is invalid", Tabs.Orientation.VERTICAL,
                tabs.getOrientation());
    }

    @Test
    public void selectTabByReference() {
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tab tab3 = new Tab("Tab three");
        Tabs tabs = new Tabs(tab1, tab2, tab3);

        tabs.setSelectedTab(tab2);

        Assert.assertEquals("Selected tab is invalid", tab2,
                tabs.getSelectedTab());
        Assert.assertEquals("Selected index is invalid", 1,
                tabs.getSelectedIndex());
    }

    @Test
    public void selectTabByIndex() {
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tab tab3 = new Tab("Tab three");
        Tabs tabs = new Tabs(tab1, tab2, tab3);

        tabs.setSelectedIndex(2);

        Assert.assertEquals("Selected tab is invalid", tab3,
                tabs.getSelectedTab());
        Assert.assertEquals("Selected index is invalid", 2,
                tabs.getSelectedIndex());
    }

    @Test
    public void selectInvalidIndex_previousIndexIsReverted() {
        Tab tab = new Tab("Tab");
        Tabs tabs = new Tabs(tab);

        // Select index out of range
        tabs.setSelectedIndex(10);
        Assert.assertEquals(0, tabs.getSelectedIndex());

        // Deselect the active tab
        tabs.setSelectedIndex(-1);
        // Select index out of range
        tabs.setSelectedIndex(10);
        Assert.assertEquals(-1, tabs.getSelectedIndex());
    }

    @Test
    public void selectInvalidIndex_warningIsShown() {
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
    public void shouldThrowWhenTabToSelectIsNotChild() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Tab to select must be a child: Tab{orphan}");
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tab tab3 = new Tab("orphan");
        Tabs tabs = new Tabs(tab1, tab2);

        tabs.setSelectedTab(tab3);

        // Exception expected - nothing to assert
    }

    @Test
    public void setFlexGrowForEnclosedTabs() {
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tab tab3 = new Tab("Tab three");
        Tabs tabs = new Tabs(tab1, tab2, tab3);

        tabs.setFlexGrowForEnclosedTabs(1.5);

        Assert.assertEquals("flexGrow of tab1 is invalid", 1.5,
                tab1.getFlexGrow(), 0.0);
        Assert.assertEquals("flexGrow of tab2 is invalid", 1.5,
                tab2.getFlexGrow(), 0.0);
        Assert.assertEquals("flexGrow of tab3 is invalid", 1.5,
                tab3.getFlexGrow(), 0.0);
    }

    @Test
    public void shouldThrowOnNegativeFlexGrow() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Flex grow property must not be negative");
        Tabs tabs = new Tabs();

        tabs.setFlexGrowForEnclosedTabs(-1);

        // Exception expected - nothing to assert
    }

    @Test
    public void selectTab_tabIsSelected() {
        Tabs tabs = new Tabs();
        Tab tab1 = new Tab("foo");
        Tab tab2 = new Tab("foo");
        tabs.add(tab1, tab2);

        tabs.setSelectedTab(tab2);

        Assert.assertFalse(tab1.isSelected());
        Assert.assertTrue(tab2.isSelected());
    }

    @Test
    public void tabsAutoselectConstructor() {
        Tabs tabs1 = new Tabs(true);
        tabs1.add(new Tab("Tab"));
        Assert.assertEquals(tabs1.getSelectedIndex(), 0);

        Tabs tabs2 = new Tabs(false);
        tabs2.add(new Tab("Tab"));
        Assert.assertEquals(tabs2.getSelectedIndex(), -1);
    }

    @Test
    public void tabsWithoutAutomaticSelection() {
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tabs tabs2 = new Tabs(false, tab1, tab2);

        Assert.assertNull(tabs2.getSelectedTab());
        Assert.assertEquals(tabs2.getSelectedIndex(), -1);
    }

    @Test
    public void removeTabInTabsWithoutAutomaticSelection() {
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tab tab3 = new Tab("Tab three");
        Tabs tabs = new Tabs(false, tab1, tab2, tab3);

        tabs.setSelectedTab(tab2);
        tabs.remove(tab1);

        Assert.assertEquals("should not change selected tab",
                tabs.getSelectedTab(), tab2);

        tabs.remove(tab2);
        Assert.assertNull("should not select other tab if current tab removed",
                tabs.getSelectedTab());
    }

    @Test
    public void removeTabInTabsWithoutSelection() {
        Tab tab1 = new Tab("Tab one");
        Tabs tabs = new Tabs(tab1);

        tabs.setSelectedTab(null);
        tabs.remove(tab1);

        Assert.assertNull("should not change selected tab",
                tabs.getSelectedTab());
    }

    @Test
    public void addTabsAsComponentArray_noClassCastExceptionIsThrown() {
        Tabs tabs = new Tabs();
        Component[] tabsArray = { new Tab(), new Tab() };

        // we test that the following call does not fail with ClassCastException
        tabs.add(tabsArray);

        // assertion here just to make sure tabs were really set
        Assert.assertNotNull(tabs.getSelectedTab());
    }

    @Test
    public void addTabsToDisabledContainer_reEnable_tabsShouldBeEnabled() {
        var container = new Div();
        var tabs = new Tabs();
        container.add(tabs);
        var tab1 = new Tab("Tab one");
        var tab2 = new Tab("Tab two");

        container.setEnabled(false);
        tabs.add(tab1, tab2);
        container.setEnabled(true);

        Assert.assertTrue(tab1.isEnabled());
        Assert.assertTrue(tab2.isEnabled());
    }

    @Test
    public void addTabsToDisabledContainer_reEnable_shouldHaveSelectedTab() {
        var container = new Div();
        var tabs = new Tabs();
        container.add(tabs);
        var tab1 = new Tab("Tab one");
        var tab2 = new Tab("Tab two");

        container.setEnabled(false);
        tabs.add(tab1, tab2);
        container.setEnabled(true);

        Assert.assertEquals(tab1, tabs.getSelectedTab());
    }

    @Test
    public void addDisabledTab_shouldNotHaveSelectedTab() {
        var tabs = new Tabs();
        var tab1 = new Tab("Tab one");

        tab1.setEnabled(false);
        tabs.add(tab1);
        tabs.setSelectedIndex(0);

        Assert.assertEquals(null, tabs.getSelectedTab());
    }
}
