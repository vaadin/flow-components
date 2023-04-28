/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.sidenav.tests;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;

import static com.vaadin.flow.component.sidenav.tests.SideNavTest.SetLabelOption.SET_LABEL_AFTER_ITEMS_CREATION;
import static com.vaadin.flow.component.sidenav.tests.SideNavTest.SetLabelOption.SET_LABEL_BEFORE_ITEMS_CREATION;
import static com.vaadin.flow.component.sidenav.tests.SideNavTest.SetLabelOption.SET_LABEL_DURING_ITEMS_CREATION;
import static com.vaadin.flow.component.sidenav.tests.SideNavTest.SetLabelOption.SET_NO_LABEL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

public class SideNavTest {

    private SideNav sideNav;

    @Before
    public void setup() {
        sideNav = new SideNav();
    }

    @Test
    public void setCollapsible_isCollapsible() {
        sideNav.setCollapsible(true);

        assertThat(sideNav.isCollapsible(), is(true));
    }

    @Test
    public void setNotCollapsible_isNotCollapsible() {
        sideNav.setCollapsible(false);

        assertThat(sideNav.isCollapsible(), is(false));
    }

    @Test
    public void changeLabel_labelChanged() {
        Assert.assertNull(sideNav.getLabel());
        sideNav.setLabel("Navigation test");

        assertThat(sideNav.getLabel(), equalTo("Navigation test"));
    }

    @Test
    public void createdWithLabel_labelIsSet() {
        final SideNav nav = new SideNav("Test label");

        assertThat(nav.getLabel(), equalTo("Test label"));
    }

    @Test
    public void addSingleItem_itemAdded() {
        assertThat(sideNav.getElement().getChildCount(), equalTo(0));

        sideNav.addItem(new SideNavItem("Test"));

        assertThat(sideNav.getElement().getChildCount(), equalTo(1));
    }

    @Test
    public void addTwoItemsAtOnce_itemsAdded() {
        assertThat(sideNav.getElement().getChildCount(), equalTo(0));

        sideNav.addItem(new SideNavItem("Test1"), new SideNavItem("Test2"));

        assertThat(sideNav.getElement().getChildCount(), equalTo(2));
    }

    @Test
    public void noItems_addItemAsFirst_itemIsAdded() {
        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAsFirst(testItem);

        assertThat(sideNav.getItems(), hasSize(1));
        assertThat(sideNav.getItems().get(0), equalTo(testItem));
    }

    @Test
    public void noItemsWithLabelSet_addItemAsFirst_itemIsAdded() {
        sideNav.setLabel("Test label");

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAsFirst(testItem);

        assertThat(sideNav.getItems(), hasSize(1));
        assertThat(sideNav.getItems().get(0), equalTo(testItem));
    }

    @Test
    public void multipleItemsAndNoLabel_addItemAsFirst_itemIsFirst() {
        List<SideNavItem> initialItems = setupItemsAndLabel(SET_NO_LABEL);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAsFirst(testItem);

        assertThat(sideNav.getItems(), hasSize(initialItems.size() + 1));
        assertThat(sideNav.getItems().get(0), equalTo(testItem));
    }

    @Test
    public void multipleItemsAndLabelBefore_addItemAsFirst_itemIsFirst() {
        List<SideNavItem> initialItems = setupItemsAndLabel(
                SET_LABEL_BEFORE_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAsFirst(testItem);

        assertThat(sideNav.getItems(), hasSize(initialItems.size() + 1));
        assertThat(sideNav.getItems().get(0), equalTo(testItem));
    }

    @Test
    public void multipleItemsAndLabelDuring_addItemAsFirst_itemIsFirst() {
        List<SideNavItem> initialItems = setupItemsAndLabel(
                SET_LABEL_DURING_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAsFirst(testItem);

        assertThat(sideNav.getItems(), hasSize(initialItems.size() + 1));
        assertThat(sideNav.getItems().get(0), equalTo(testItem));
    }

    @Test
    public void multipleItemsAndLabelAfter_addItemAsFirst_itemIsFirst() {
        List<SideNavItem> initialItems = setupItemsAndLabel(
                SET_LABEL_AFTER_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAsFirst(testItem);

        assertThat(sideNav.getItems(), hasSize(initialItems.size() + 1));
        assertThat(sideNav.getItems().get(0), equalTo(testItem));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addItemAtNegativeIndex_throws() {
        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(-1, testItem);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noItems_addItemAtTooHighIndex_throws() {
        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(1, testItem);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addItemAtTooHighIndex_throws() {
        final List<SideNavItem> items = setupItemsAndLabel(
                SET_LABEL_BEFORE_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(items.size() + 1, testItem);
    }

    @Test
    public void noItems_addItemAtIndexZero_itemIsAdded() {
        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(0, testItem);

        assertThat(sideNav.getItems(), hasSize(1));
        assertThat(sideNav.getItems().get(0), equalTo(testItem));
    }

    @Test
    public void noItemsLabelSet_addItemAtIndexZero_itemIsAdded() {
        sideNav.setLabel("Test label");

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(0, testItem);

        assertThat(sideNav.getItems(), hasSize(1));
        assertThat(sideNav.getItems().get(0), equalTo(testItem));
    }

    @Test
    public void multipleItemsAndNoLabel_addItemAtIndex_itemIsAdded() {
        final List<SideNavItem> items = setupItemsAndLabel(SET_NO_LABEL);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(2, testItem);

        assertThat(sideNav.getItems(), hasSize(items.size() + 1));
        assertThat(sideNav.getItems().get(2), equalTo(testItem));
    }

    @Test
    public void multipleItemsAndNoLabel_addItemAtLastIndex_itemIsAppended() {
        final List<SideNavItem> items = setupItemsAndLabel(SET_NO_LABEL);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(sideNav.getItems().size(), testItem);

        assertThat(sideNav.getItems(), hasSize(items.size() + 1));
        assertThat(sideNav.getItems().get(sideNav.getItems().size() - 1),
                equalTo(testItem));
    }

    @Test
    public void multipleItemsAndLabelBefore_addItemAtIndex_itemIsAdded() {
        final List<SideNavItem> items = setupItemsAndLabel(
                SET_LABEL_BEFORE_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(2, testItem);

        assertThat(sideNav.getItems(), hasSize(items.size() + 1));
        assertThat(sideNav.getItems().get(2), equalTo(testItem));
    }

    @Test
    public void multipleItemsAndLabelBefore_addItemAtLastIndex_itemIsAppended() {
        final List<SideNavItem> items = setupItemsAndLabel(
                SET_LABEL_BEFORE_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(sideNav.getItems().size(), testItem);

        assertThat(sideNav.getItems(), hasSize(items.size() + 1));
        assertThat(sideNav.getItems().get(sideNav.getItems().size() - 1),
                equalTo(testItem));
    }

    @Test
    public void multipleItemsAndLabelAfter_addItemAtIndex_itemIsAdded() {
        final List<SideNavItem> items = setupItemsAndLabel(
                SET_LABEL_AFTER_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(2, testItem);

        assertThat(sideNav.getItems(), hasSize(items.size() + 1));
        assertThat(sideNav.getItems().get(2), equalTo(testItem));
    }

    @Test
    public void multipleItemsAndLabelAfter_addItemAtLastIndex_itemIsAppended() {
        final List<SideNavItem> items = setupItemsAndLabel(
                SET_LABEL_AFTER_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(sideNav.getItems().size(), testItem);

        assertThat(sideNav.getItems(), hasSize(items.size() + 1));
        assertThat(sideNav.getItems().get(sideNav.getItems().size() - 1),
                equalTo(testItem));
    }

    @Test
    public void multipleItemsAndLabelDuring_addItemAtIndexBeforeLabel_itemIsAddedAtCorrectPosition() {
        final List<SideNavItem> items = setupItemsAndLabel(
                SET_LABEL_DURING_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(1, testItem);

        assertThat(sideNav.getItems(), hasSize(items.size() + 1));
        assertThat(sideNav.getItems().get(1), equalTo(testItem));
    }

    @Test
    public void multipleItemsAndLabelAfter_addItemAtIndexAfterLabel_itemIsAddedAtCorrectPosition() {
        final List<SideNavItem> items = setupItemsAndLabel(
                SET_LABEL_DURING_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(2, testItem);

        assertThat(sideNav.getItems(), hasSize(items.size() + 1));
        assertThat(sideNav.getItems().get(2), equalTo(testItem));
    }

    @Test
    public void multipleItems_removeAll_allItemsRemoved() {
        setupItemsAndLabel(SET_NO_LABEL);

        sideNav.removeAll();

        assertThat(sideNav.getItems(), is(empty()));
    }

    @Test
    public void removeAll_labelStillSet() {
        setupItemsAndLabel(SET_LABEL_BEFORE_ITEMS_CREATION);

        sideNav.removeAll();

        assertThat(sideNav.getLabel(), not(isEmptyString()));
    }

    @Test
    public void removeSingleItem_itemRemoved() {
        final List<SideNavItem> sideNavItems = setupItemsAndLabel(
                SET_LABEL_BEFORE_ITEMS_CREATION);

        sideNav.remove(sideNavItems.get(2));

        assertThat(sideNav.getItems(), hasSize(sideNavItems.size() - 1));
        assertThat(sideNav.getItems(), not(hasItem(sideNavItems.get(2))));
    }

    @Test
    public void removeTwoItems_bothItemsRemoved() {
        final List<SideNavItem> sideNavItems = setupItemsAndLabel(
                SET_LABEL_BEFORE_ITEMS_CREATION);

        sideNav.remove(sideNavItems.get(1), sideNavItems.get(2));

        assertThat(sideNav.getItems(), hasSize(sideNavItems.size() - 2));
        assertThat(sideNav.getItems(), not(hasItem(sideNavItems.get(1))));
        assertThat(sideNav.getItems(), not(hasItem(sideNavItems.get(2))));
    }

    @Test
    public void removeUnknownItem_nothingHappens() {
        final List<SideNavItem> sideNavItems = setupItemsAndLabel(
                SET_LABEL_BEFORE_ITEMS_CREATION);

        sideNav.remove(new SideNavItem("Foreign item"));

        assertThat(sideNav.getItems(), contains(sideNavItems.toArray()));
    }

    enum SetLabelOption {
        SET_NO_LABEL, SET_LABEL_BEFORE_ITEMS_CREATION, SET_LABEL_DURING_ITEMS_CREATION, SET_LABEL_AFTER_ITEMS_CREATION
    }

    private List<SideNavItem> setupItemsAndLabel(
            SetLabelOption setLabelOption) {
        List<SideNavItem> items = new ArrayList<>();

        if (setLabelOption == SET_LABEL_BEFORE_ITEMS_CREATION) {
            sideNav.setLabel("Test label");
        }

        addNavItem("Item1", "http://localhost:8080/item1", items);
        addNavItem("Item2", "http://localhost:8080/item2", items);

        if (setLabelOption == SET_LABEL_DURING_ITEMS_CREATION) {
            sideNav.setLabel("Test label");
        }

        addNavItem("Item3", "http://localhost:8080/item3", items);
        addNavItem("Item4", "http://localhost:8080/item4", items);

        if (setLabelOption == SET_LABEL_AFTER_ITEMS_CREATION) {
            sideNav.setLabel("Test label");
        }

        return items;
    }

    private void addNavItem(String Item1, String url, List<SideNavItem> items) {
        SideNavItem item = new SideNavItem(Item1, url);
        items.add(item);
        sideNav.addItem(item);
    }

}
