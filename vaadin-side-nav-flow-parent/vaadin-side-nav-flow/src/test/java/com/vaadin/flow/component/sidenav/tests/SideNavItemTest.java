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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.sidenav.SideNavItem;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyString;

public class SideNavItemTest {

    private SideNavItem sideNavItem;

    @Before
    public void setup() {
        sideNavItem = new SideNavItem("Item", "/path");
    }

    @Test
    public void changeLabel_labelChanged() {
        Assert.assertEquals(sideNavItem.getLabel(), "Item");
        sideNavItem.setLabel("Item Changed");
        Assert.assertEquals(sideNavItem.getLabel(), "Item Changed");
    }

    @Test
    public void createWithNoPath_pathNotSet() {
        final SideNavItem item = new SideNavItem("Test");

        assertThat(item.getPath(), is(nullValue()));
    }

    @Test
    public void setNullStringPath_pathAttributeRemoved() {
        sideNavItem.setPath((String) null);

        assertThat(sideNavItem.getElement().hasAttribute("path"), is(false));
        assertThat(sideNavItem.getPath(), nullValue());
    }

    @Test
    public void setNullComponentPath_pathAttributeRemoved() {
        sideNavItem.setPath((Class<? extends Component>) null);

        assertThat(sideNavItem.getElement().hasAttribute("path"), is(false));
        assertThat(sideNavItem.getPath(), nullValue());
    }

    @Test
    public void setEmptyPath_returnsEmptyPath() {
        final SideNavItem item = new SideNavItem("Test");
        item.setPath("");

        assertThat(item.getPath(), equalTo(""));
        assertThat(sideNavItem.getElement().hasAttribute("path"), is(true));
    }

    @Test
    public void returnsExpectedPath() {
        assertThat(sideNavItem.getPath(), equalTo("/path"));
    }

    @Test
    public void addSingleItem_itemAdded() {
        // one child for the label element
        assertThat(sideNavItem.getElement().getChildCount(), equalTo(1));

        sideNavItem.addItem(new SideNavItem("Test"));

        assertThat(sideNavItem.getElement().getChildCount(), equalTo(2));
    }

    @Test
    public void addSingleItem_itemHasCorrectSlot() {
        sideNavItem.addItem(new SideNavItem("Test"));

        assertThat(sideNavItem.getElement().getChild(1).getAttribute("slot"),
                equalTo("children"));
    }

    @Test
    public void addTwoItemsAtOnce_itemsAdded() {
        // one child for the label element
        assertThat(sideNavItem.getElement().getChildCount(), equalTo(1));

        sideNavItem.addItem(new SideNavItem("Test1"), new SideNavItem("Test2"));

        assertThat(sideNavItem.getElement().getChildCount(), equalTo(3));
    }

    @Test
    public void noItems_addItemAsFirst_itemIsAdded() {
        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAsFirst(testItem);

        assertThat(sideNavItem.getItems(), hasSize(1));
        assertThat(sideNavItem.getItems().get(0), equalTo(testItem));
    }

    @Test
    public void multipleItems_addItemAsFirst_itemIsFirst() {
        List<SideNavItem> initialItems = setupItems();

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAsFirst(testItem);

        assertThat(sideNavItem.getItems(), hasSize(initialItems.size() + 1));
        assertThat(sideNavItem.getItems().get(0), equalTo(testItem));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addItemAtNegativeIndex_throws() {
        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(-1, testItem);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noItems_addItemAtTooHighIndex_throws() {
        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(1, testItem);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addItemAtTooHighIndex_throws() {
        final List<SideNavItem> items = setupItems();

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(items.size() + 1, testItem);
    }

    @Test
    public void noItems_addItemAtIndexZero_itemIsAdded() {
        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(0, testItem);

        assertThat(sideNavItem.getItems(), hasSize(1));
        assertThat(sideNavItem.getItems().get(0), equalTo(testItem));
    }

    @Test
    public void multipleItems_addItemAtIndex_itemIsAdded() {
        final List<SideNavItem> items = setupItems();

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(2, testItem);

        assertThat(sideNavItem.getItems(), hasSize(items.size() + 1));
        assertThat(sideNavItem.getItems().get(2), equalTo(testItem));
    }

    @Test
    public void multipleItems_addItemAtIndex_itemHasCorrectSlot() {
        setupItems();

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(2, testItem);

        assertThat(testItem.getElement().getAttribute("slot"),
                equalTo("children"));
    }

    @Test
    public void multipleItemsPrefixAndSuffix_addItemAtIndex_addedItemHasCorrectPosition() {
        final List<SideNavItem> items = setupItemsPrefixAndSuffix();

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(2, testItem);

        assertThat(sideNavItem.getItems(), hasSize(items.size() + 1));
        assertThat(sideNavItem.getItems().get(2), equalTo(testItem));
    }

    @Test
    public void multipleItemsPrefixAndSuffix_addItemAtHigherIndex_addedItemHasCorrectPosition() {
        final List<SideNavItem> items = setupItemsPrefixAndSuffix();

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(3, testItem);

        assertThat(sideNavItem.getItems(), hasSize(items.size() + 1));
        assertThat(sideNavItem.getItems().get(3), equalTo(testItem));
    }

    @Test
    public void multipleItems_addItemAtLastIndex_itemIsAppended() {
        final List<SideNavItem> items = setupItems();

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(sideNavItem.getItems().size(), testItem);

        assertThat(sideNavItem.getItems(), hasSize(items.size() + 1));
        assertThat(
                sideNavItem.getItems().get(sideNavItem.getItems().size() - 1),
                equalTo(testItem));
    }

    @Test
    public void multipleItems_removeAll_allItemsRemoved() {
        setupItems();

        sideNavItem.removeAll();

        assertThat(sideNavItem.getItems(), is(empty()));
    }

    @Test
    public void removeAll_labelStillSet() {
        setupItems();

        sideNavItem.removeAll();

        assertThat(sideNavItem.getLabel(), not(isEmptyString()));
    }

    @Test
    public void removeAll_prefixAndSuffixStillSet() {
        setupItemsPrefixAndSuffix();

        sideNavItem.removeAll();

        assertThat(sideNavItem.getPrefixComponent(), notNullValue());
        assertThat(sideNavItem.getSuffixComponent(), notNullValue());
    }

    @Test
    public void removeSingleItem_itemRemoved() {
        final List<SideNavItem> sideNavItems = setupItems();

        sideNavItem.remove(sideNavItems.get(2));

        assertThat(sideNavItem.getItems(), hasSize(sideNavItems.size() - 1));
        assertThat(sideNavItem.getItems(), not(hasItem(sideNavItems.get(2))));
    }

    @Test
    public void removeTwoItems_bothItemsRemoved() {
        final List<SideNavItem> sideNavItems = setupItems();

        sideNavItem.remove(sideNavItems.get(1), sideNavItems.get(2));

        assertThat(sideNavItem.getItems(), hasSize(sideNavItems.size() - 2));
        assertThat(sideNavItem.getItems(), not(hasItem(sideNavItems.get(1))));
        assertThat(sideNavItem.getItems(), not(hasItem(sideNavItems.get(2))));
    }

    @Test
    public void removeUnknownItem_nothingHappens() {
        final List<SideNavItem> sideNavItems = setupItems();

        sideNavItem.remove(new SideNavItem("Foreign item"));

        assertThat(sideNavItem.getItems(), contains(sideNavItems.toArray()));
    }

    @Test
    public void createWithPathAndPrefix_pathAndPrefixIsSet() {
        final Div prefixComponent = new Div();
        final SideNavItem item = new SideNavItem("Test item", "test-path",
                prefixComponent);

        assertThat(item.getPath(), equalTo("test-path"));
        assertThat(item.getPrefixComponent(), equalTo(prefixComponent));
    }

    @Test
    public void isCollapsedByDefault() {
        assertThat(sideNavItem.isExpanded(), equalTo(false));
    }

    @Test
    public void setExpanded_isExpanded() {
        sideNavItem.setExpanded(true);

        assertThat(sideNavItem.isExpanded(), equalTo(true));
        assertThat(sideNavItem.getElement().hasAttribute("expanded"),
                equalTo(true));
    }

    @Test
    public void expandAndCollapse_isCollapsed() {
        sideNavItem.setExpanded(true);
        sideNavItem.setExpanded(false);

        assertThat(sideNavItem.isExpanded(), equalTo(false));
        assertThat(sideNavItem.getElement().hasAttribute("expanded"),
                equalTo(false));
    }

    private List<SideNavItem> setupItems() {
        List<SideNavItem> items = new ArrayList<>();

        addNavItem("Item1", "http://localhost:8080/item1", items);
        addNavItem("Item2", "http://localhost:8080/item2", items);
        addNavItem("Item3", "http://localhost:8080/item3", items);
        addNavItem("Item4", "http://localhost:8080/item4", items);

        return items;
    }

    private List<SideNavItem> setupItemsPrefixAndSuffix() {
        List<SideNavItem> items = new ArrayList<>();

        addNavItem("Item1", "http://localhost:8080/item1", items);
        sideNavItem.setPrefixComponent(new Div());
        addNavItem("Item2", "http://localhost:8080/item2", items);
        addNavItem("Item3", "http://localhost:8080/item3", items);
        sideNavItem.setSuffixComponent(new Div());
        addNavItem("Item4", "http://localhost:8080/item4", items);

        return items;
    }

    private void addNavItem(String Item1, String url, List<SideNavItem> items) {
        SideNavItem item = new SideNavItem(Item1, url);
        items.add(item);
        sideNavItem.addItem(item);
    }

}
