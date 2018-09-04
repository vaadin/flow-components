/*
 * Copyright 2000-2017 Vaadin Ltd.
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
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;

/**
 * @author Vaadin Ltd.
 */
public class SelectionEventTest {

    private Tabs tabs;
    private Tab tab1;
    private Tab tab2;

    private int eventCount;

    @Before
    public void init() {
        tab1 = new Tab("foo");
        tab2 = new Tab("bar");
        tabs = new Tabs(tab1, tab2);

        tabs.addSelectedChangeListener(e -> {
            Assert.assertFalse(
                    "isFromClient() returned true for an event fired from server",
                    e.isFromClient());
            eventCount++;
        });

        eventCount = 0;
    }

    @Test
    public void changeSelectionInServerSide_eventFiredSynchronously() {
        tabs.setSelectedTab(tab2);
        Assert.assertEquals(
                "Selection event should have been fired immediately after "
                        + "changing the selection on server side.",
                1, eventCount);

        tabs.setSelectedTab(tab2);
        Assert.assertEquals("The tab was already selected, selection event "
                + "should not have been fired.", 1, eventCount);

        tabs.setSelectedIndex(0);
        Assert.assertEquals(
                "Selection event should have been fired immediately after "
                        + "changing the selection on server side.",
                2, eventCount);

        tabs.setSelectedIndex(0);
        Assert.assertEquals("The tab was already selected, selection event "
                + "should not have been fired.", 2, eventCount);
    }

    @Test
    public void removeSelectedTab_selectionChanged() {
        tabs.remove(tab1);
        Assert.assertEquals(
                "Selection event should have been fired after removing the selected tab",
                1, eventCount);
        Assert.assertEquals(
                "The next tab should be selected after removing the selected tab",
                tabs.getSelectedTab(), tab2);
    }

    @Test
    public void removeLaterTab_selectionNotChanged() {
        tabs.remove(tab2);
        Assert.assertEquals(
                "Selection event should not have been fired after removing the other tab",
                0, eventCount);
        Assert.assertEquals("The selected tab should not have been changed",
                tabs.getSelectedTab(), tab1);
    }

    @Test
    public void removeEarlierTab_selectionNotChanged() {
        tabs.setSelectedTab(tab2);
        Assert.assertEquals(1, eventCount);
        tabs.remove(tab1);

        Assert.assertEquals(
                "Selection event should not have been fired after removing the other tab",
                1, eventCount);

        Assert.assertEquals(
                "The selected index should have been reduced to keep the old selection",
                0, tabs.getSelectedIndex());

        Assert.assertEquals("The selected tab should not have been changed",
                tabs.getSelectedTab(), tab2);
    }

    @Test
    public void removeAllTabs_selectionChangedToNull_selectedIndexMinusOne() {
        tabs.remove(tab1, tab2);

        Assert.assertEquals(
                "Selection event should have been fired after removing all the tabs",
                1, eventCount);

        Assert.assertEquals(
                "The selected index should be -1 when there are no tabs", -1,
                tabs.getSelectedIndex());

        Assert.assertEquals(
                "The selected tab should be null after removing all tabs",
                tabs.getSelectedTab(), null);
    }

    @Test
    public void selectSecondTab_removeAll_selectionChangedToNull_selectedIndexMinusOne() {
        tabs.setSelectedIndex(1);
        Assert.assertEquals(1, eventCount);

        tabs.removeAll();

        Assert.assertEquals(
                "Selection event should have been fired after removing all the tabs",
                2, eventCount);

        Assert.assertEquals(
                "The selected index should be -1 when there are no tabs", -1,
                tabs.getSelectedIndex());

        Assert.assertEquals(
                "The selected tab should be null after removing all tabs",
                tabs.getSelectedTab(), null);
    }

    @Test
    public void addTabs_selectionNotChanged() {
        tabs.addComponentAsFirst(new Tab());
        tabs.add(new Tab());

        Assert.assertEquals(
                "Selection event should not have been fired after adding new tabs",
                0, eventCount);

        Assert.assertEquals(
                "Selection should not have been changed after adding new tabs",
                tab1, tabs.getSelectedTab());

        Assert.assertEquals(
                "Selected index should have been incremented after adding new tab in the beginning",
                1, tabs.getSelectedIndex());
    }

    @Test
    public void selectLastTab_removeLastTab_secondLastTabIsSelected() {
        tabs.setSelectedTab(tab2);
        Assert.assertEquals(1, eventCount);
        tabs.remove(tab2);
        Assert.assertEquals(
                "Selection event should have been fired after removing the selected tab",
                2, eventCount);
        Assert.assertEquals(
                "The new last tab should be selected after removing the last tab which was selected",
                tab1, tabs.getSelectedTab());
    }

    @Test
    public void replaceSelectedTab_eventIsFired_newTabIsSelected() {
        Tab replaceTab = new Tab("replace");
        tabs.replace(tab1, replaceTab);
        Assert.assertEquals(
                "Selection event should have been fired after replacing the selected tab",
                1, eventCount);
        Assert.assertEquals(
                "After replacing the selected tab, the new tab should be selected",
                replaceTab, tabs.getSelectedTab());
    }

    @Test
    public void removeNonChildTab_selectionNotChanged() {
        tabs.setSelectedTab(tab2);
        Assert.assertEquals(1, eventCount);
        Tab orphan = new Tab();
        tabs.remove(orphan);
        Assert.assertEquals(
                "Selection event should not have been fired after "
                        + "removing a Tab which is not a child of the Tabs.",
                1, eventCount);
        Assert.assertEquals(
                "Selected tab should not have been changed after "
                        + "removing a Tab which is not a child of the Tabs.",
                tab2, tabs.getSelectedTab());
    }

    @Test
    public void unselect_eventFired_selectedIndexMinusOne() {
        tabs.setSelectedTab(null);
        Assert.assertEquals("Unselecting the selected tab should fire event", 1,
                eventCount);
        Assert.assertEquals("The selected tab should be null after unselecting",
                null, tabs.getSelectedTab());
        Assert.assertEquals("The selected index is -1 when no tab is selected",
                -1, tabs.getSelectedIndex());
    }

    @Test
    public void unselectWithIndex() {
        tabs.setSelectedIndex(-1);
        Assert.assertEquals("Unselecting the selected tab should fire event", 1,
                eventCount);
        Assert.assertEquals("The selected tab should be null after unselecting",
                null, tabs.getSelectedTab());
    }

    @Test
    public void unselectWithAnyNegativeIndex_selectedIndexMinusOne() {
        tabs.setSelectedIndex(-100);
        Assert.assertEquals("Unselecting the selected tab should fire event", 1,
                eventCount);
        Assert.assertEquals("The selected tab should be null after unselecting",
                null, tabs.getSelectedTab());
        Assert.assertEquals(
                "The selected index should return -1 when no tab is selected, even "
                        + "when the index was set as some other negative number",
                -1, tabs.getSelectedIndex());
    }

    @Test
    public void unselect_selectOldSelection_eventFired() {
        tabs.setSelectedTab(null);
        tabs.setSelectedTab(tab1);
        Assert.assertEquals("Selection event should have been fired", 2,
                eventCount);
        Assert.assertEquals("Selected tab should be the one which was selected",
                tab1, tabs.getSelectedTab());
    }

    @Test
    public void unselectMultipleTimes_noEvent() {
        tabs.setSelectedTab(null);
        Assert.assertEquals(1, eventCount);
        tabs.setSelectedTab(null);
        Assert.assertEquals(
                "Selection was not changed, no event should've been fired", 1,
                eventCount);
        tabs.setSelectedIndex(-1);
        Assert.assertEquals(
                "Selection was not changed, no event should've been fired", 1,
                eventCount);
    }

}
