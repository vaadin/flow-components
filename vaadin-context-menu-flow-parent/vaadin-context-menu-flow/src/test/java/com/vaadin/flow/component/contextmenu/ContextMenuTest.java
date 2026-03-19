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
package com.vaadin.flow.component.contextmenu;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;

/**
 * Unit tests for the ContextMenu.
 */
class ContextMenuTest {

    @Test
    void createContextMenuWithTargetAndChildren_getChildrenReturnsChildren() {
        Span span1 = new Span("Text 1");
        Span span2 = new Span("Text 2");
        Span span3 = new Span("Text 3");

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setTarget(new Span("target"));

        contextMenu.addComponent(span1, span2);

        List<Component> children = contextMenu.getChildren()
                .collect(Collectors.toList());
        Assertions.assertEquals(2, children.size());
        Assertions.assertTrue(children.contains(span1));
        Assertions.assertTrue(children.contains(span2));

        contextMenu.addComponent(span3);
        children = contextMenu.getChildren().collect(Collectors.toList());
        Assertions.assertEquals(3, children.size());
        Assertions.assertTrue(children.contains(span1));
        Assertions.assertTrue(children.contains(span2));
        Assertions.assertTrue(children.contains(span3));

        contextMenu.remove(span2);
        children = contextMenu.getChildren().collect(Collectors.toList());
        Assertions.assertEquals(2, children.size());
        Assertions.assertTrue(children.contains(span1));
        Assertions.assertTrue(children.contains(span3));

        contextMenu.remove(span1);
        children = contextMenu.getChildren().collect(Collectors.toList());
        Assertions.assertEquals(1, children.size());
        Assertions.assertTrue(children.contains(span3));

        contextMenu.removeAll();
        children = contextMenu.getChildren().collect(Collectors.toList());
        Assertions.assertEquals(0, children.size());
    }

    @Test
    void setTarget_removeAll_targetNotMoved() {
        Span target = new Span("target");
        Div div = new Div(target);

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setTarget(target);

        Assertions.assertEquals(div, target.getParent().get(),
                "The target component should be attached to its original parent");

        contextMenu.removeAll();

        Assertions.assertEquals(div, target.getParent().get(),
                "The target component should be attached to its original parent");
    }

    @Test
    void addItem_getChildren_returnsMenuItem() {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.addItem("foo", null);
        List<Component> children = contextMenu.getChildren()
                .collect(Collectors.toList());
        Assertions.assertEquals(1, children.size());
        assertComponentIsMenuItem(children.get(0), "foo");
    }

    @Test
    void addItem_getItems_returnsMenuItem() {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.addItem("foo", null);
        List<MenuItem> children = contextMenu.getItems();
        Assertions.assertEquals(1, children.size());
        assertComponentIsMenuItem(children.get(0), "foo");
    }

    @Test
    void addItem_remove_noChildrenNorItems() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem item = contextMenu.addItem("foo", null);
        contextMenu.remove(item);
        Assertions.assertEquals(0, contextMenu.getChildren().count());
        Assertions.assertEquals(0, contextMenu.getItems().size());
    }

    @Test
    void addItemsAndComponents_getChildrenReturnsAllInOrder() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem item1 = contextMenu.addItem("foo", null);

        Span span1 = new Span("foo");
        contextMenu.addComponent(span1);

        MenuItem item2 = contextMenu.addItem("bar", null);

        Span span2 = new Span("bar");
        contextMenu.addComponent(span2);

        List<Component> children = contextMenu.getChildren()
                .collect(Collectors.toList());
        Assertions.assertEquals(4, children.size());

        Assertions.assertEquals(item1, children.get(0));
        Assertions.assertEquals(span1, children.get(1));
        Assertions.assertEquals(item2, children.get(2));
        Assertions.assertEquals(span2, children.get(3));
    }

    @Test
    void addItemsAndSeparator_separatorOnlyIncludedInChildren() {
        var contextMenu = new ContextMenu();
        var item1 = contextMenu.addItem("foo", null);
        contextMenu.addSeparator();
        var item2 = contextMenu.addItem("bar", null);

        var children = contextMenu.getChildren().toList();
        var items = contextMenu.getItems();
        Assertions.assertEquals(3, children.size());
        Assertions.assertEquals(2, items.size());
        Assertions.assertEquals(item1, children.get(0));
        Assertions.assertEquals(item2, children.get(2));
        Assertions.assertEquals(item1, items.get(0));
        Assertions.assertEquals(item2, items.get(1));
    }

    @Test
    void addItemsAndComponents_getItemsReturnsItemsOnly() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem item1 = contextMenu.addItem("foo", null);

        Span span1 = new Span("foo");
        contextMenu.addComponent(span1);

        MenuItem item2 = contextMenu.addItem("bar", null);

        Span span2 = new Span("bar");
        contextMenu.addComponent(span2);

        List<MenuItem> items = contextMenu.getItems();
        Assertions.assertEquals(2, items.size());
        Assertions.assertEquals(item1, items.get(0));
        Assertions.assertEquals(item2, items.get(1));
    }

    @Test
    void addComponentAtIndex_negativeIndex() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> addDivAtIndex(-1));
    }

    @Test
    void addComponentAtIndex_indexIsBiggerThanChildrenCount() {
        Assertions.assertThrows(IndexOutOfBoundsException.class,
                () -> addDivAtIndex(1));
    }

    @Test
    void addItemsWithNullClickListeners_doesNotThrow() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem foo = contextMenu.addItem("foo", null);
        contextMenu.addItem(new Div(), null);

        foo.getSubMenu().addItem("bar", null);
        foo.getSubMenu().addItem(new Div(), null);
    }

    @Test
    void serializeContextMenu() throws IOException {
        NativeButton menuButton = new NativeButton();
        ContextMenu menu = new ContextMenu(menuButton);
        menu.addComponent(new Span());
        ObjectOutputStream out = new ObjectOutputStream(
                new ByteArrayOutputStream());
        out.writeObject(menu);
    }

    private void addDivAtIndex(int index) {
        ContextMenu contextMenu = new ContextMenu();

        Div div = new Div();
        contextMenu.addComponentAtIndex(index, div);
    }

    private void assertComponentIsMenuItem(Component component,
            String expectedText) {
        Assertions.assertTrue(MenuItem.class.isInstance(component),
                "Component is not an instance of " + MenuItem.class.getName());
        Assertions.assertEquals(expectedText, ((MenuItem) component).getText());
    }

    @Test
    void unregisterOpenedChangeListenerOnEvent() {
        ContextMenu contextMenu = new ContextMenu();
        AtomicInteger listenerInvokedCount = new AtomicInteger(0);
        contextMenu.addOpenedChangeListener(e -> {
            listenerInvokedCount.incrementAndGet();
            e.unregisterListener();
        });

        ComponentUtil.fireEvent(contextMenu,
                new ContextMenuBase.OpenedChangeEvent<>(contextMenu, false));
        ComponentUtil.fireEvent(contextMenu,
                new ContextMenuBase.OpenedChangeEvent<>(contextMenu, false));

        Assertions.assertEquals(1, listenerInvokedCount.get());
    }

    @Test
    void setPosition_getPosition() {
        var targetDiv = new Div();
        var contextMenu = new ContextMenu(targetDiv);
        contextMenu.setPosition(ContextMenuPosition.END);
        Assertions.assertEquals(ContextMenuPosition.END.getPosition(),
                contextMenu.getElement().getProperty("position"));
        Assertions.assertEquals(ContextMenuPosition.END,
                contextMenu.getPosition());
    }

    @Test
    void defaultPosition_equalsNull() {
        var targetDiv = new Div();
        var contextMenu = new ContextMenu(targetDiv);

        Assertions.assertNull(contextMenu.getPosition());
    }
}
