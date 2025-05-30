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
package com.vaadin.flow.component.contextmenu;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;

/**
 * Unit tests for SubMenu.
 */
public class SubMenuTest {

    private ContextMenu contextMenu;
    private MenuItem parentItem;
    private SubMenu subMenu;

    @Before
    public void setup() {
        contextMenu = new ContextMenu();
        parentItem = contextMenu.addItem("parent");
        subMenu = parentItem.getSubMenu();
    }

    @Test
    public void addAndRemoveChildren_getChildrenReturnsChildren() {
        Span span1 = new Span("Text 1");
        Span span2 = new Span("Text 2");
        Span span3 = new Span("Text 3");

        subMenu.addComponent(span1, span2);
        verifyChildren(subMenu, span1, span2);

        subMenu.addComponentAtIndex(1, span3);
        verifyChildren(subMenu, span1, span3, span2);

        subMenu.remove(span3);
        verifyChildren(subMenu, span1, span2);

        subMenu.remove(span1);
        verifyChildren(subMenu, span2);

        subMenu.removeAll();
        verifyChildren(subMenu);
    }

    @Test
    public void addItem_getChildren_returnsMenuItem() {
        MenuItem foo = subMenu.addItem("foo");
        verifyChildren(subMenu, foo);
    }

    @Test
    public void addSeparatorAddsHr() {
        MenuItem foo = subMenu.addItem("foo");
        subMenu.addSeparator();
        Hr separator = (Hr) subMenu.getChildren().skip(1).findFirst().get();
        MenuItem bar = subMenu.addItem("foo");
        verifyChildren(subMenu, foo, separator, bar);
    }

    @Test
    public void addItem_getItems_returnsMenuItem() {
        MenuItem foo = subMenu.addItem("foo");
        verifyItems(subMenu, foo);
    }

    @Test
    public void addItem_remove_noChildrenNorItems() {
        MenuItem foo = subMenu.addItem("foo");
        subMenu.remove(foo);
        verifyChildren(subMenu);
        verifyItems(subMenu);
    }

    @Test
    public void addItemsAndComponents_getChildrenReturnsAllInOrder() {
        MenuItem item1 = subMenu.addItem("foo");

        Span span1 = new Span("foo");
        subMenu.addComponent(span1);

        MenuItem item2 = subMenu.addItem("bar");

        Span span2 = new Span("bar");
        subMenu.addComponent(span2);

        verifyChildren(subMenu, item1, span1, item2, span2);
    }

    @Test
    public void addItemsAndComponents_getItemsReturnsItemsOnly() {
        MenuItem item1 = subMenu.addItem("foo");

        Span span1 = new Span("foo");
        subMenu.addComponent(span1);

        MenuItem item2 = subMenu.addItem("bar");

        Span span2 = new Span("bar");
        subMenu.addComponent(span2);

        verifyItems(subMenu, item1, item2);
    }

    @Test
    public void addItem_notIncludedInContextMenuItemsNorComponents() {
        subMenu.addItem("foo");

        Assert.assertEquals(1, contextMenu.getChildren().count());
        Assert.assertSame(parentItem,
                contextMenu.getChildren().findFirst().get());

        Assert.assertEquals(1, contextMenu.getItems().size());
        Assert.assertSame(parentItem, contextMenu.getItems().get(0));
    }

    @Test
    public void menuItem_isParentItem_returnsTrueOnlyWhenSubMenuHasContent() {
        Assert.assertFalse(parentItem.isParentItem());

        subMenu.addItem("foo");
        Assert.assertTrue(parentItem.isParentItem());
        subMenu.removeAll();
        Assert.assertFalse(parentItem.isParentItem());

        subMenu.addComponent(new Span());
        Assert.assertTrue(parentItem.isParentItem());
        subMenu.removeAll();
        Assert.assertFalse(parentItem.isParentItem());
    }

    @Test
    public void subMenuItem_getContextMenu_returnsContextMenu() {
        MenuItem foo = subMenu.addItem("foo");
        Assert.assertEquals(contextMenu, foo.getContextMenu());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addComponentAtIndex_negativeIndex_throws() {
        subMenu.addComponentAtIndex(-1, new Div());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void addComponentAtIndex_indexIsBiggerThanChildrenCount_throws() {
        subMenu.addComponentAtIndex(1, new Div());
    }

    @Test(expected = IllegalStateException.class)
    public void setParentItemCheckable_throws() {
        subMenu.addItem("foo");
        parentItem.setCheckable(true);
    }

    @Test(expected = IllegalStateException.class)
    public void setParentItemKeepOpen_throws() {
        subMenu.addItem("foo");
        parentItem.setKeepOpen(true);
    }

    @Test
    public void addToCheckableItemSubMenu_throws() {
        parentItem.setCheckable(true);

        Stream<Consumer<SubMenu>> addOperations = Stream.of(
        //@formatter:off
                menu -> menu.addComponent(new Div()),
                menu -> menu.addItem("foo"),
                menu -> menu.addItem(new Div()),
                menu -> menu.addItem("foo", e -> {}),
                menu -> menu.addItem(new Div(), e -> {}),
                menu -> menu.addComponentAtIndex(0, new Div()));
        //@formatter:on

        addOperations.forEach(operation -> {
            try {
                operation.accept(subMenu);
                Assert.fail(
                        "Should throw IllegalStateException when adding content "
                                + "to a sub menu with a checkable parent item.");
            } catch (IllegalStateException e) {
                // expected
            }
        });
    }

    @Test
    public void getSubMenu_returnsAlwaysSameInstance() {
        subMenu.addItem("foo");
        Assert.assertSame(subMenu, parentItem.getSubMenu());
    }

    private void verifyChildren(SubMenu subMenu,
            Component... expectedChildren) {
        List<Component> children = subMenu.getChildren()
                .collect(Collectors.toList());
        Assert.assertArrayEquals(expectedChildren,
                children.toArray(new Component[children.size()]));
    }

    private void verifyItems(SubMenu subMenu, MenuItem... expectedItems) {
        List<MenuItem> items = subMenu.getItems();
        Assert.assertArrayEquals(expectedItems,
                items.toArray(new MenuItem[items.size()]));
    }

}
