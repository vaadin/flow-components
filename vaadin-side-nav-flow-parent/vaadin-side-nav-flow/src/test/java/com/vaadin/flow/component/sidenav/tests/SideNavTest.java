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
package com.vaadin.flow.component.sidenav.tests;

import static com.vaadin.flow.component.sidenav.tests.SideNavTest.SetLabelOption.SET_LABEL_AFTER_ITEMS_CREATION;
import static com.vaadin.flow.component.sidenav.tests.SideNavTest.SetLabelOption.SET_LABEL_BEFORE_ITEMS_CREATION;
import static com.vaadin.flow.component.sidenav.tests.SideNavTest.SetLabelOption.SET_LABEL_DURING_ITEMS_CREATION;
import static com.vaadin.flow.component.sidenav.tests.SideNavTest.SetLabelOption.SET_NO_LABEL;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.dom.Element;

class SideNavTest {

    private SideNav sideNav;

    @BeforeEach
    void setup() {
        sideNav = new SideNav();
    }

    @Test
    void setCollapsible_isCollapsible() {
        sideNav.setCollapsible(true);

        Assertions.assertTrue(sideNav.isCollapsible());
    }

    @Test
    void setNotCollapsible_isNotCollapsible() {
        sideNav.setCollapsible(false);

        Assertions.assertFalse(sideNav.isCollapsible());
    }

    @Test
    void setAutoExpand_isAutoExpand() {
        Assertions.assertTrue(sideNav.isAutoExpand());
        Assertions.assertFalse(
                sideNav.getElement().getProperty("noAutoExpand", false));

        sideNav.setAutoExpand(false);

        Assertions.assertFalse(sideNav.isAutoExpand());
        Assertions.assertTrue(
                sideNav.getElement().getProperty("noAutoExpand", false));
    }

    @Test
    void changeLabel_labelChanged() {
        Assertions.assertNull(sideNav.getLabel());
        sideNav.setLabel("Navigation test");

        Assertions.assertEquals("Navigation test", sideNav.getLabel());
    }

    @Test
    void setLabel_labelElementPresent() {
        Assertions.assertFalse(sideNavHasLabelElement());
        sideNav.setLabel("Navigation test");

        Assertions.assertTrue(sideNavHasLabelElement());
    }

    @Test
    void setLabelAndUnsetLabel_labelElementRemoved() {
        sideNav.setLabel("Navigation test");
        sideNav.setLabel(null);

        Assertions.assertFalse(sideNavHasLabelElement());
    }

    private boolean sideNavHasLabelElement() {
        return sideNav.getElement().getChildren()
                .anyMatch(this::isLabelElement);
    }

    private boolean isLabelElement(Element element) {
        return Objects.equals(element.getAttribute("slot"), "label");
    }

    @Test
    void setCollapsed_isCollapsed() {
        sideNav.setExpanded(false);

        Assertions.assertFalse(sideNav.isExpanded());
    }

    @Test
    void collapseAndExpand_isExpanded() {
        sideNav.setExpanded(false);
        sideNav.setExpanded(true);

        Assertions.assertTrue(sideNav.isExpanded());
    }

    @Test
    void setLabelToNull_labelIsNull() {
        sideNav.setLabel("Navigation test");
        sideNav.setLabel(null);

        Assertions.assertNull(sideNav.getLabel());
    }

    @Test
    void setEmptyLabel_labelIsEmpty() {
        sideNav.setLabel("");

        Assertions.assertEquals("", sideNav.getLabel());
    }

    @Test
    void createdWithLabel_labelIsSet() {
        final SideNav nav = new SideNav("Test label");

        Assertions.assertEquals("Test label", nav.getLabel());
    }

    @Test
    void addSingleItem_itemAdded() {
        Assertions.assertEquals(0, sideNav.getElement().getChildCount());

        sideNav.addItem(new SideNavItem("Test"));

        Assertions.assertEquals(1, sideNav.getElement().getChildCount());
    }

    @Test
    void addTwoItemsAtOnce_itemsAdded() {
        Assertions.assertEquals(0, sideNav.getElement().getChildCount());

        sideNav.addItem(new SideNavItem("Test1"), new SideNavItem("Test2"));

        Assertions.assertEquals(2, sideNav.getElement().getChildCount());
    }

    @Test
    void noItems_addItemAsFirst_itemIsAdded() {
        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAsFirst(testItem);

        Assertions.assertEquals(1, sideNav.getItems().size());
        Assertions.assertEquals(testItem, sideNav.getItems().get(0));
    }

    @Test
    void noItemsWithLabelSet_addItemAsFirst_itemIsAdded() {
        sideNav.setLabel("Test label");

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAsFirst(testItem);

        Assertions.assertEquals(1, sideNav.getItems().size());
        Assertions.assertEquals(testItem, sideNav.getItems().get(0));
    }

    @Test
    void multipleItemsAndNoLabel_addItemAsFirst_itemIsFirst() {
        List<SideNavItem> initialItems = setupItemsAndLabel(SET_NO_LABEL);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAsFirst(testItem);

        Assertions.assertEquals(initialItems.size() + 1,
                sideNav.getItems().size());
        Assertions.assertEquals(testItem, sideNav.getItems().get(0));
    }

    @Test
    void multipleItemsAndLabelBefore_addItemAsFirst_itemIsFirst() {
        List<SideNavItem> initialItems = setupItemsAndLabel(
                SET_LABEL_BEFORE_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAsFirst(testItem);

        Assertions.assertEquals(initialItems.size() + 1,
                sideNav.getItems().size());
        Assertions.assertEquals(testItem, sideNav.getItems().get(0));
    }

    @Test
    void multipleItemsAndLabelDuring_addItemAsFirst_itemIsFirst() {
        List<SideNavItem> initialItems = setupItemsAndLabel(
                SET_LABEL_DURING_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAsFirst(testItem);

        Assertions.assertEquals(initialItems.size() + 1,
                sideNav.getItems().size());
        Assertions.assertEquals(testItem, sideNav.getItems().get(0));
    }

    @Test
    void multipleItemsAndLabelAfter_addItemAsFirst_itemIsFirst() {
        List<SideNavItem> initialItems = setupItemsAndLabel(
                SET_LABEL_AFTER_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAsFirst(testItem);

        Assertions.assertEquals(initialItems.size() + 1,
                sideNav.getItems().size());
        Assertions.assertEquals(testItem, sideNav.getItems().get(0));
    }

    @Test
    void addItemAtNegativeIndex_throws() {
        final SideNavItem testItem = new SideNavItem("testItem");
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> sideNav.addItemAtIndex(-1, testItem));
    }

    @Test
    void noItems_addItemAtTooHighIndex_throws() {
        final SideNavItem testItem = new SideNavItem("testItem");
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> sideNav.addItemAtIndex(1, testItem));
    }

    @Test
    void addItemAtTooHighIndex_throws() {
        final List<SideNavItem> items = setupItemsAndLabel(
                SET_LABEL_BEFORE_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> sideNav.addItemAtIndex(items.size() + 1, testItem));
    }

    @Test
    void noItems_addItemAtIndexZero_itemIsAdded() {
        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(0, testItem);

        Assertions.assertEquals(1, sideNav.getItems().size());
        Assertions.assertEquals(testItem, sideNav.getItems().get(0));
    }

    @Test
    void noItemsLabelSet_addItemAtIndexZero_itemIsAdded() {
        sideNav.setLabel("Test label");

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(0, testItem);

        Assertions.assertEquals(1, sideNav.getItems().size());
        Assertions.assertEquals(testItem, sideNav.getItems().get(0));
    }

    @Test
    void multipleItemsAndNoLabel_addItemAtIndex_itemIsAdded() {
        final List<SideNavItem> items = setupItemsAndLabel(SET_NO_LABEL);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(2, testItem);

        Assertions.assertEquals(items.size() + 1, sideNav.getItems().size());
        Assertions.assertEquals(testItem, sideNav.getItems().get(2));
    }

    @Test
    void multipleItemsAndNoLabel_addItemAtLastIndex_itemIsAppended() {
        final List<SideNavItem> items = setupItemsAndLabel(SET_NO_LABEL);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(sideNav.getItems().size(), testItem);

        Assertions.assertEquals(items.size() + 1, sideNav.getItems().size());
        Assertions.assertEquals(testItem,
                sideNav.getItems().get(sideNav.getItems().size() - 1));
    }

    @Test
    void multipleItemsAndLabelBefore_addItemAtIndex_itemIsAdded() {
        final List<SideNavItem> items = setupItemsAndLabel(
                SET_LABEL_BEFORE_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(2, testItem);

        Assertions.assertEquals(items.size() + 1, sideNav.getItems().size());
        Assertions.assertEquals(testItem, sideNav.getItems().get(2));
    }

    @Test
    void multipleItemsAndLabelBefore_addItemAtLastIndex_itemIsAppended() {
        final List<SideNavItem> items = setupItemsAndLabel(
                SET_LABEL_BEFORE_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(sideNav.getItems().size(), testItem);

        Assertions.assertEquals(items.size() + 1, sideNav.getItems().size());
        Assertions.assertEquals(testItem,
                sideNav.getItems().get(sideNav.getItems().size() - 1));
    }

    @Test
    void multipleItemsAndLabelAfter_addItemAtIndex_itemIsAdded() {
        final List<SideNavItem> items = setupItemsAndLabel(
                SET_LABEL_AFTER_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(2, testItem);

        Assertions.assertEquals(items.size() + 1, sideNav.getItems().size());
        Assertions.assertEquals(testItem, sideNav.getItems().get(2));
    }

    @Test
    void multipleItemsAndLabelAfter_addItemAtLastIndex_itemIsAppended() {
        final List<SideNavItem> items = setupItemsAndLabel(
                SET_LABEL_AFTER_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(sideNav.getItems().size(), testItem);

        Assertions.assertEquals(items.size() + 1, sideNav.getItems().size());
        Assertions.assertEquals(testItem,
                sideNav.getItems().get(sideNav.getItems().size() - 1));
    }

    @Test
    void multipleItemsAndLabelDuring_addItemAtIndexBeforeLabel_itemIsAddedAtCorrectPosition() {
        final List<SideNavItem> items = setupItemsAndLabel(
                SET_LABEL_DURING_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(1, testItem);

        Assertions.assertEquals(items.size() + 1, sideNav.getItems().size());
        Assertions.assertEquals(testItem, sideNav.getItems().get(1));
    }

    @Test
    void multipleItemsAndLabelAfter_addItemAtIndexAfterLabel_itemIsAddedAtCorrectPosition() {
        final List<SideNavItem> items = setupItemsAndLabel(
                SET_LABEL_DURING_ITEMS_CREATION);

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNav.addItemAtIndex(2, testItem);

        Assertions.assertEquals(items.size() + 1, sideNav.getItems().size());
        Assertions.assertEquals(testItem, sideNav.getItems().get(2));
    }

    @Test
    void multipleItems_removeAll_allItemsRemoved() {
        setupItemsAndLabel(SET_NO_LABEL);

        sideNav.removeAll();

        Assertions.assertTrue(sideNav.getItems().isEmpty());
    }

    @Test
    void removeAll_labelStillSet() {
        setupItemsAndLabel(SET_LABEL_BEFORE_ITEMS_CREATION);

        sideNav.removeAll();

        Assertions.assertFalse(sideNav.getLabel().isEmpty());
    }

    @Test
    void removeSingleItem_itemRemoved() {
        final List<SideNavItem> sideNavItems = setupItemsAndLabel(
                SET_LABEL_BEFORE_ITEMS_CREATION);

        sideNav.remove(sideNavItems.get(2));

        Assertions.assertEquals(sideNavItems.size() - 1,
                sideNav.getItems().size());
        Assertions
                .assertFalse(sideNav.getItems().contains(sideNavItems.get(2)));
    }

    @Test
    void removeTwoItems_bothItemsRemoved() {
        final List<SideNavItem> sideNavItems = setupItemsAndLabel(
                SET_LABEL_BEFORE_ITEMS_CREATION);

        sideNav.remove(sideNavItems.get(1), sideNavItems.get(2));

        Assertions.assertEquals(sideNavItems.size() - 2,
                sideNav.getItems().size());
        Assertions
                .assertFalse(sideNav.getItems().contains(sideNavItems.get(1)));
        Assertions
                .assertFalse(sideNav.getItems().contains(sideNavItems.get(2)));
    }

    @Test
    void removeUnknownItem_nothingHappens() {
        final List<SideNavItem> sideNavItems = setupItemsAndLabel(
                SET_LABEL_BEFORE_ITEMS_CREATION);

        sideNav.remove(new SideNavItem("Foreign item"));

        Assertions.assertEquals(sideNav.getItems(), sideNavItems);
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions.assertTrue(
                HasThemeVariant.class.isAssignableFrom(SideNav.class));
    }

    enum SetLabelOption {
        SET_NO_LABEL,
        SET_LABEL_BEFORE_ITEMS_CREATION,
        SET_LABEL_DURING_ITEMS_CREATION,
        SET_LABEL_AFTER_ITEMS_CREATION
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
