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

public class SideNavTest {

    private SideNav sideNav;

    @Before
    public void setup() {
        sideNav = new SideNav();
    }

    @Test
    public void setCollapsible_isCollapsible() {
        sideNav.setCollapsible(true);

        Assert.assertTrue(sideNav.isCollapsible());
    }

    @Test
    public void setNotCollapsible_isNotCollapsible() {
        sideNav.setCollapsible(false);

        Assert.assertFalse(sideNav.isCollapsible());
    }

    @Test
    public void changeLabel_labelChanged() {
        Assert.assertNull(sideNav.getLabel());
        sideNav.setLabel("Navigation test");

        Assert.assertEquals("Navigation test", sideNav.getLabel());
    }

    @Test
    public void createdWithLabel_labelIsSet() {
        final SideNav nav = new SideNav("Test label");

        Assert.assertEquals("Test label", nav.getLabel());
    }

    @Test
    public void addSingleItem_itemAdded() {
        Assert.assertEquals(0, sideNav.getElement().getChildCount());

        sideNav.addItem(new SideNavItem("Test"));

        Assert.assertEquals(1, sideNav.getElement().getChildCount());
    }

    @Test
    public void addTwoItemsAtOnce_itemsAdded() {
        Assert.assertEquals(0, sideNav.getElement().getChildCount());

        sideNav.addItem(new SideNavItem("Test1"), new SideNavItem("Test2"));

        Assert.assertEquals(2, sideNav.getElement().getChildCount());
    }

    @Test
    public void noItems_addItemAsFirst_itemIsAdded() {
        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAsFirst(testItem);

        Assert.assertEquals(1, sideNav.getItems().size());
        Assert.assertEquals(testItem, sideNav.getItems().get(0));
    }

    @Test
    public void noItemsWithLabelSet_addItemAsFirst_itemIsAdded() {
        sideNav.setLabel("Test label");

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAsFirst(testItem);

        Assert.assertEquals(1, sideNav.getItems().size());
        Assert.assertEquals(testItem, sideNav.getItems().get(0));
    }

    @Test
    public void multipleItemsAndNoLabel_addItemAsFirst_itemIsFirst() {
        List<SideNavItem> initialItems = setupItemsAndLabel(SET_NO_LABEL);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAsFirst(testItem);

        Assert.assertEquals(initialItems.size() + 1, sideNav.getItems().size());
        Assert.assertEquals(testItem, sideNav.getItems().get(0));
    }

    @Test
    public void multipleItemsAndLabelBefore_addItemAsFirst_itemIsFirst() {
        List<SideNavItem> initialItems = setupItemsAndLabel(
                SET_LABEL_BEFORE_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAsFirst(testItem);

        Assert.assertEquals(initialItems.size() + 1, sideNav.getItems().size());
        Assert.assertEquals(testItem, sideNav.getItems().get(0));
    }

    @Test
    public void multipleItemsAndLabelDuring_addItemAsFirst_itemIsFirst() {
        List<SideNavItem> initialItems = setupItemsAndLabel(
                SET_LABEL_DURING_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAsFirst(testItem);

        Assert.assertEquals(initialItems.size() + 1, sideNav.getItems().size());
        Assert.assertEquals(testItem, sideNav.getItems().get(0));
    }

    @Test
    public void multipleItemsAndLabelAfter_addItemAsFirst_itemIsFirst() {
        List<SideNavItem> initialItems = setupItemsAndLabel(
                SET_LABEL_AFTER_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAsFirst(testItem);

        Assert.assertEquals(initialItems.size() + 1, sideNav.getItems().size());
        Assert.assertEquals(testItem, sideNav.getItems().get(0));
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

        Assert.assertEquals(1, sideNav.getItems().size());
        Assert.assertEquals(testItem, sideNav.getItems().get(0));
    }

    @Test
    public void noItemsLabelSet_addItemAtIndexZero_itemIsAdded() {
        sideNav.setLabel("Test label");

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(0, testItem);

        Assert.assertEquals(1, sideNav.getItems().size());
        Assert.assertEquals(testItem, sideNav.getItems().get(0));
    }

    @Test
    public void multipleItemsAndNoLabel_addItemAtIndex_itemIsAdded() {
        final List<SideNavItem> items = setupItemsAndLabel(SET_NO_LABEL);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(2, testItem);

        Assert.assertEquals(items.size() + 1, sideNav.getItems().size());
        Assert.assertEquals(testItem, sideNav.getItems().get(2));
    }

    @Test
    public void multipleItemsAndNoLabel_addItemAtLastIndex_itemIsAppended() {
        final List<SideNavItem> items = setupItemsAndLabel(SET_NO_LABEL);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(sideNav.getItems().size(), testItem);

        Assert.assertEquals(items.size() + 1, sideNav.getItems().size());
        Assert.assertEquals(testItem,
                sideNav.getItems().get(sideNav.getItems().size() - 1));
    }

    @Test
    public void multipleItemsAndLabelBefore_addItemAtIndex_itemIsAdded() {
        final List<SideNavItem> items = setupItemsAndLabel(
                SET_LABEL_BEFORE_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(2, testItem);

        Assert.assertEquals(items.size() + 1, sideNav.getItems().size());
        Assert.assertEquals(testItem, sideNav.getItems().get(2));
    }

    @Test
    public void multipleItemsAndLabelBefore_addItemAtLastIndex_itemIsAppended() {
        final List<SideNavItem> items = setupItemsAndLabel(
                SET_LABEL_BEFORE_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(sideNav.getItems().size(), testItem);

        Assert.assertEquals(items.size() + 1, sideNav.getItems().size());
        Assert.assertEquals(testItem,
                sideNav.getItems().get(sideNav.getItems().size() - 1));
    }

    @Test
    public void multipleItemsAndLabelAfter_addItemAtIndex_itemIsAdded() {
        final List<SideNavItem> items = setupItemsAndLabel(
                SET_LABEL_AFTER_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(2, testItem);

        Assert.assertEquals(items.size() + 1, sideNav.getItems().size());
        Assert.assertEquals(testItem, sideNav.getItems().get(2));
    }

    @Test
    public void multipleItemsAndLabelAfter_addItemAtLastIndex_itemIsAppended() {
        final List<SideNavItem> items = setupItemsAndLabel(
                SET_LABEL_AFTER_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(sideNav.getItems().size(), testItem);

        Assert.assertEquals(items.size() + 1, sideNav.getItems().size());
        Assert.assertEquals(testItem,
                sideNav.getItems().get(sideNav.getItems().size() - 1));
    }

    @Test
    public void multipleItemsAndLabelDuring_addItemAtIndexBeforeLabel_itemIsAddedAtCorrectPosition() {
        final List<SideNavItem> items = setupItemsAndLabel(
                SET_LABEL_DURING_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(1, testItem);

        Assert.assertEquals(items.size() + 1, sideNav.getItems().size());
        Assert.assertEquals(testItem, sideNav.getItems().get(1));
    }

    @Test
    public void multipleItemsAndLabelAfter_addItemAtIndexAfterLabel_itemIsAddedAtCorrectPosition() {
        final List<SideNavItem> items = setupItemsAndLabel(
                SET_LABEL_DURING_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(2, testItem);

        Assert.assertEquals(items.size() + 1, sideNav.getItems().size());
        Assert.assertEquals(testItem, sideNav.getItems().get(2));
    }

    @Test
    public void multipleItems_removeAll_allItemsRemoved() {
        setupItemsAndLabel(SET_NO_LABEL);

        sideNav.removeAll();

        Assert.assertTrue(sideNav.getItems().isEmpty());
    }

    @Test
    public void removeAll_labelStillSet() {
        setupItemsAndLabel(SET_LABEL_BEFORE_ITEMS_CREATION);

        sideNav.removeAll();

        Assert.assertFalse(sideNav.getLabel().isEmpty());
    }

    @Test
    public void removeSingleItem_itemRemoved() {
        final List<SideNavItem> sideNavItems = setupItemsAndLabel(
                SET_LABEL_BEFORE_ITEMS_CREATION);

        sideNav.remove(sideNavItems.get(2));

        Assert.assertEquals(sideNavItems.size() - 1, sideNav.getItems().size());
        Assert.assertFalse(sideNav.getItems().contains(sideNavItems.get(2)));
    }

    @Test
    public void removeTwoItems_bothItemsRemoved() {
        final List<SideNavItem> sideNavItems = setupItemsAndLabel(
                SET_LABEL_BEFORE_ITEMS_CREATION);

        sideNav.remove(sideNavItems.get(1), sideNavItems.get(2));

        Assert.assertEquals(sideNavItems.size() - 2, sideNav.getItems().size());
        Assert.assertFalse(sideNav.getItems().contains(sideNavItems.get(1)));
        Assert.assertFalse(sideNav.getItems().contains(sideNavItems.get(2)));
    }

    @Test
    public void removeUnknownItem_nothingHappens() {
        final List<SideNavItem> sideNavItems = setupItemsAndLabel(
                SET_LABEL_BEFORE_ITEMS_CREATION);

        sideNav.remove(new SideNavItem("Foreign item"));

        Assert.assertEquals(sideNav.getItems(), sideNavItems);
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
