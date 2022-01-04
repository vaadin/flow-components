/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Vaadin Ltd.
 */
public class TabsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void createTabsInDefaultState() {
        Tabs tabs = new Tabs();

        assertThat("Initial child count is invalid", tabs.getComponentCount(),
                is(0));
        assertThat("Initial orientation is invalid", tabs.getOrientation(),
                is(Tabs.Orientation.HORIZONTAL));
        assertThat("Initial selected index is invalid", tabs.getSelectedIndex(),
                is(-1));
        assertThat("Initial child count is invalid", tabs.getSelectedTab(),
                CoreMatchers.nullValue());
    }

    @Test
    public void createTabsWithChildren() {
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tab tab3 = new Tab("Tab three");
        Tabs tabs = new Tabs(tab1, tab2, tab3);

        assertThat("Initial child count is invalid", tabs.getComponentCount(),
                is(3));
        assertThat("Initial orientation is invalid", tabs.getOrientation(),
                is(Tabs.Orientation.HORIZONTAL));
        assertThat("Initial selected tab is invalid", tabs.getSelectedTab(),
                is(tab1));
        assertThat("Initial selected index is invalid", tabs.getSelectedIndex(),
                is(0));
    }

    @Test
    public void setOrientation() {
        Tabs tabs = new Tabs();

        tabs.setOrientation(Tabs.Orientation.VERTICAL);

        assertThat("Orientation is invalid", tabs.getOrientation(),
                is(Tabs.Orientation.VERTICAL));
    }

    @Test
    public void selectTabByReference() {
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tab tab3 = new Tab("Tab three");
        Tabs tabs = new Tabs(tab1, tab2, tab3);

        tabs.setSelectedTab(tab2);

        assertThat("Selected tab is invalid", tabs.getSelectedTab(), is(tab2));
        assertThat("Selected index is invalid", tabs.getSelectedIndex(), is(1));
    }

    @Test
    public void selectTabByIndex() {
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tab tab3 = new Tab("Tab three");
        Tabs tabs = new Tabs(tab1, tab2, tab3);

        tabs.setSelectedIndex(2);

        assertThat("Selected tab is invalid", tabs.getSelectedTab(), is(tab3));
        assertThat("Selected index is invalid", tabs.getSelectedIndex(), is(2));
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

        assertThat("flexGrow of tab1 is invalid", tab1.getFlexGrow(), is(1.5));
        assertThat("flexGrow of tab2 is invalid", tab2.getFlexGrow(), is(1.5));
        assertThat("flexGrow of tab3 is invalid", tab3.getFlexGrow(), is(1.5));
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
}
