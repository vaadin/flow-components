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

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;

/**
 * @author Vaadin Ltd.
 */
class SelectionEventTest {

    private Tabs tabs;
    private Tab tab1;
    private Tab tab2;

    private int eventCount;

    @BeforeEach
    void setup() {
        tab1 = new Tab("foo");
        tab2 = new Tab("bar");
        tabs = new Tabs(tab1, tab2);

        addSelectedChangeListener(tabs);

        eventCount = 0;
    }

    private void addSelectedChangeListener(Tabs tabs) {
        tabs.addSelectedChangeListener(e -> {
            Assertions.assertFalse(e.isFromClient(),
                    "isFromClient() returned true for an event fired from server");
            eventCount++;
        });
    }

    @Test
    void changeSelectionInServerSide_eventFiredSynchronously() {
        tabs.setSelectedTab(tab2);
        Assertions.assertEquals(1, eventCount,
                "Selection event should have been fired immediately after "
                        + "changing the selection on server side.");

        tabs.setSelectedTab(tab2);
        Assertions.assertEquals(1, eventCount,
                "The tab was already selected, selection event "
                        + "should not have been fired.");

        tabs.setSelectedIndex(0);
        Assertions.assertEquals(2, eventCount,
                "Selection event should have been fired immediately after "
                        + "changing the selection on server side.");

        tabs.setSelectedIndex(0);
        Assertions.assertEquals(2, eventCount,
                "The tab was already selected, selection event "
                        + "should not have been fired.");
    }

    @Test
    void removeSelectedTab_selectionChanged() {
        tabs.remove(tab1);
        Assertions.assertEquals(1, eventCount,
                "Selection event should have been fired after removing the selected tab");
        Assertions.assertEquals(tabs.getSelectedTab(), tab2,
                "The next tab should be selected after removing the selected tab");
    }

    @Test
    void removeLaterTab_selectionNotChanged() {
        tabs.remove(tab2);
        Assertions.assertEquals(0, eventCount,
                "Selection event should not have been fired after removing the other tab");
        Assertions.assertEquals(tabs.getSelectedTab(), tab1,
                "The selected tab should not have been changed");
    }

    @Test
    void removeEarlierTab_selectionNotChanged() {
        tabs.setSelectedTab(tab2);
        Assertions.assertEquals(1, eventCount);
        tabs.remove(tab1);

        Assertions.assertEquals(1, eventCount,
                "Selection event should not have been fired after removing the other tab");

        Assertions.assertEquals(0, tabs.getSelectedIndex(),
                "The selected index should have been reduced to keep the old selection");

        Assertions.assertEquals(tabs.getSelectedTab(), tab2,
                "The selected tab should not have been changed");
    }

    @Test
    void removeAllTabs_selectionChangedToNull_selectedIndexMinusOne() {
        tabs.remove(tab1, tab2);

        Assertions.assertEquals(1, eventCount,
                "Selection event should have been fired after removing all the tabs");

        Assertions.assertEquals(-1, tabs.getSelectedIndex(),
                "The selected index should be -1 when there are no tabs");

        Assertions.assertEquals(null, tabs.getSelectedTab(),
                "The selected tab should be null after removing all tabs");
    }

    @Test
    void selectSecondTab_removeAll_selectionChangedToNull_selectedIndexMinusOne() {
        tabs.setSelectedIndex(1);
        Assertions.assertEquals(1, eventCount);

        tabs.removeAll();

        Assertions.assertEquals(2, eventCount,
                "Selection event should have been fired after removing all the tabs");

        Assertions.assertEquals(-1, tabs.getSelectedIndex(),
                "The selected index should be -1 when there are no tabs");

        Assertions.assertEquals(null, tabs.getSelectedTab(),
                "The selected tab should be null after removing all tabs");
    }

    @Test
    void addTabs_selectionNotChanged() {
        tabs.addTabAsFirst(new Tab());
        tabs.add(new Tab());

        Assertions.assertEquals(0, eventCount,
                "Selection event should not have been fired after adding new tabs");

        Assertions.assertEquals(tab1, tabs.getSelectedTab(),
                "Selection should not have been changed after adding new tabs");

        Assertions.assertEquals(1, tabs.getSelectedIndex(),
                "Selected index should have been incremented after adding new tab in the beginning");
    }

    @Test
    void selectLastTab_removeLastTab_secondLastTabIsSelected() {
        tabs.setSelectedTab(tab2);
        Assertions.assertEquals(1, eventCount);
        tabs.remove(tab2);
        Assertions.assertEquals(2, eventCount,
                "Selection event should have been fired after removing the selected tab");
        Assertions.assertEquals(tab1, tabs.getSelectedTab(),
                "The new last tab should be selected after removing the last tab which was selected");
    }

    @Test
    void replaceSelectedTab_eventIsFired_newTabIsSelected() {
        Tab replaceTab = new Tab("replace");
        tabs.replace(tab1, replaceTab);
        Assertions.assertEquals(1, eventCount,
                "Selection event should have been fired after replacing the selected tab");
        Assertions.assertEquals(replaceTab, tabs.getSelectedTab(),
                "After replacing the selected tab, the new tab should be selected");
    }

    @Test
    void removeNonChildTab_selectionNotChanged() {
        tabs.setSelectedTab(tab2);
        Assertions.assertEquals(1, eventCount);
        Tab orphan = new Tab();
        tabs.remove(orphan);
        Assertions.assertEquals(1, eventCount,
                "Selection event should not have been fired after "
                        + "removing a Tab which is not a child of the Tabs.");
        Assertions.assertEquals(tab2, tabs.getSelectedTab(),
                "Selected tab should not have been changed after "
                        + "removing a Tab which is not a child of the Tabs.");
    }

    @Test
    void unselect_eventFired_selectedIndexMinusOne() {
        tabs.setSelectedTab(null);
        Assertions.assertEquals(1, eventCount,
                "Unselecting the selected tab should fire event");
        Assertions.assertEquals(null, tabs.getSelectedTab(),
                "The selected tab should be null after unselecting");
        Assertions.assertEquals(-1, tabs.getSelectedIndex(),
                "The selected index is -1 when no tab is selected");
    }

    @Test
    void unselectWithIndex() {
        tabs.setSelectedIndex(-1);
        Assertions.assertEquals(1, eventCount,
                "Unselecting the selected tab should fire event");
        Assertions.assertEquals(null, tabs.getSelectedTab(),
                "The selected tab should be null after unselecting");
    }

    @Test
    void unselectWithAnyNegativeIndex_selectedIndexMinusOne() {
        tabs.setSelectedIndex(-100);
        Assertions.assertEquals(1, eventCount,
                "Unselecting the selected tab should fire event");
        Assertions.assertEquals(null, tabs.getSelectedTab(),
                "The selected tab should be null after unselecting");
        Assertions.assertEquals(-1, tabs.getSelectedIndex(),
                "The selected index should return -1 when no tab is selected, even "
                        + "when the index was set as some other negative number");
    }

    @Test
    void unselect_selectOldSelection_eventFired() {
        tabs.setSelectedTab(null);
        tabs.setSelectedTab(tab1);
        Assertions.assertEquals(2, eventCount,
                "Selection event should have been fired");
        Assertions.assertEquals(tab1, tabs.getSelectedTab(),
                "Selected tab should be the one which was selected");
    }

    @Test
    void unselectMultipleTimes_noEvent() {
        tabs.setSelectedTab(null);
        Assertions.assertEquals(1, eventCount);
        tabs.setSelectedTab(null);
        Assertions.assertEquals(1, eventCount,
                "Selection was not changed, no event should've been fired");
        tabs.setSelectedIndex(-1);
        Assertions.assertEquals(1, eventCount,
                "Selection was not changed, no event should've been fired");
    }

    @Test
    void tabsAutoselectFalse_previousAndCurrentTab() {
        AtomicReference<Tab> currentTab = new AtomicReference<>();
        AtomicReference<Tab> previousTab = new AtomicReference<>();
        tabs = new Tabs();
        tabs.setAutoselect(false);
        tabs.add(tab1, tab2);
        tabs.addSelectedChangeListener(e -> {
            currentTab.set(e.getSelectedTab());
            previousTab.set(e.getPreviousTab());
        });

        tabs.setSelectedTab(tab1);
        Assertions.assertEquals(currentTab.get(), tab1,
                "Current tab should be tab 1");
        Assertions.assertNull(previousTab.get(),
                "Previous tab should be empty");

        tabs.setSelectedTab(tab2);
        Assertions.assertEquals(currentTab.get(), tab2,
                "Current tab should be tab 2");
        Assertions.assertEquals(previousTab.get(), tab1,
                "Previous tab should be tab 1");
    }

    @Test
    void removeCurrent_withoutAutomaticSelectionResetsSelection() {
        tabs.setAutoselect(false);
        tabs.setSelectedIndex(1);
        Assertions.assertEquals(tab2, tabs.getSelectedTab());

        tabs.remove(tab2);
        Assertions.assertEquals(-1, tabs.getSelectedIndex());
    }

    @Test
    void addFirstTabWithAddComponentAtIndex_firstTabAutoselected() {
        tabs = new Tabs();
        addSelectedChangeListener(tabs);

        tabs.addTabAtIndex(0, tab1);

        Assertions.assertEquals(0, tabs.getSelectedIndex(),
                "Unexpected selected index after adding the first tab.");
        Assertions.assertEquals(tab1, tabs.getSelectedTab(),
                "Expected the first added tab to be automatically selected.");
        Assertions.assertEquals(1, eventCount,
                "Expected autoselection to fire an event.");
    }

    @Test
    void addSecondTabWithAddComponentAtIndex_selectionNotChanged() {
        tabs = new Tabs();
        addSelectedChangeListener(tabs);

        tabs.addTabAtIndex(0, tab1);
        tabs.addTabAtIndex(0, tab2);

        Assertions.assertEquals(1, tabs.getSelectedIndex(),
                "Unexpected selected index after adding the first tab.");
        Assertions.assertEquals(tab1, tabs.getSelectedTab(),
                "Expected the first added tab to be automatically selected.");
        Assertions.assertEquals(1, eventCount,
                "Expected no selection event after adding the second tab.");
    }

    @Test
    void disableAutoSelect_addFirstTabWithAddComponentAtIndex_noAutoselect() {
        tabs = new Tabs(false);
        addSelectedChangeListener(tabs);

        tabs.addTabAtIndex(0, tab1);

        Assertions.assertEquals(-1, tabs.getSelectedIndex(),
                "Unexpected selected index after adding the first tab.");
        Assertions.assertNull(tabs.getSelectedTab(),
                "Expected no tab to be automatically selected.");
        Assertions.assertEquals(0, eventCount,
                "Expected no selection event fired.");
    }

}
