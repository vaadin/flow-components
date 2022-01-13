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
package com.vaadin.flow.component.contextmenu;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.html.NativeButton;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;

/**
 * Unit tests for the ContextMenu.
 */
public class ContextMenuTest {
    @Test
    public void templateWarningSuppressed() {
        ContextMenu contextMenu = new ContextMenu();

        Assert.assertTrue("Template warning is not suppressed", contextMenu
                .getElement().hasAttribute("suppress-template-warning"));
    }

    @Test
    public void createContextMenuWithTargetAndChildren_getChildrenReturnsChildren() {
        Label label1 = new Label("Label 1");
        Label label2 = new Label("Label 2");
        Label label3 = new Label("Label 3");

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setTarget(new Label("target"));

        contextMenu.add(label1, label2);

        List<Component> children = contextMenu.getChildren()
                .collect(Collectors.toList());
        Assert.assertEquals(2, children.size());
        Assert.assertThat(children, CoreMatchers.hasItems(label1, label2));

        contextMenu.add(label3);
        children = contextMenu.getChildren().collect(Collectors.toList());
        Assert.assertEquals(3, children.size());
        Assert.assertThat(children,
                CoreMatchers.hasItems(label1, label2, label3));

        contextMenu.remove(label2);
        children = contextMenu.getChildren().collect(Collectors.toList());
        Assert.assertEquals(2, children.size());
        Assert.assertThat(children, CoreMatchers.hasItems(label1, label3));

        contextMenu.remove(label1);
        children = contextMenu.getChildren().collect(Collectors.toList());
        Assert.assertEquals(1, children.size());
        Assert.assertThat(children, CoreMatchers.hasItems(label3));

        contextMenu.removeAll();
        children = contextMenu.getChildren().collect(Collectors.toList());
        Assert.assertEquals(0, children.size());
    }

    @Test
    public void setTarget_removeAll_targetNotMoved() {
        Label target = new Label("target");
        Div div = new Div(target);

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setTarget(target);

        Assert.assertEquals(
                "The target component should be attached to its original parent",
                div, target.getParent().get());

        contextMenu.removeAll();

        Assert.assertEquals(
                "The target component should be attached to its original parent",
                div, target.getParent().get());
    }

    @Test
    public void addItem_getChildren_returnsMenuItem() {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.addItem("foo", null);
        List<Component> children = contextMenu.getChildren()
                .collect(Collectors.toList());
        Assert.assertEquals(1, children.size());
        assertComponentIsMenuItem(children.get(0), "foo");
    }

    @Test
    public void addItem_getItems_returnsMenuItem() {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.addItem("foo", null);
        List<MenuItem> children = contextMenu.getItems();
        Assert.assertEquals(1, children.size());
        assertComponentIsMenuItem(children.get(0), "foo");
    }

    @Test
    public void addItem_remove_noChildrenNorItems() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem item = contextMenu.addItem("foo", null);
        contextMenu.remove(item);
        Assert.assertEquals(0, contextMenu.getChildren().count());
        Assert.assertEquals(0, contextMenu.getItems().size());
    }

    @Test
    public void addItemsAndComponents_getChildrenReturnsAllInOrder() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem item1 = contextMenu.addItem("foo", null);

        Label label1 = new Label("foo");
        contextMenu.add(label1);

        MenuItem item2 = contextMenu.addItem("bar", null);

        Label label2 = new Label("bar");
        contextMenu.add(label2);

        List<Component> children = contextMenu.getChildren()
                .collect(Collectors.toList());
        Assert.assertEquals(4, children.size());

        Assert.assertEquals(item1, children.get(0));
        Assert.assertEquals(label1, children.get(1));
        Assert.assertEquals(item2, children.get(2));
        Assert.assertEquals(label2, children.get(3));
    }

    @Test
    public void addItemsAndComponents_getItemsReturnsItemsOnly() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem item1 = contextMenu.addItem("foo", null);

        Label label1 = new Label("foo");
        contextMenu.add(label1);

        MenuItem item2 = contextMenu.addItem("bar", null);

        Label label2 = new Label("bar");
        contextMenu.add(label2);

        List<MenuItem> items = contextMenu.getItems();
        Assert.assertEquals(2, items.size());
        Assert.assertEquals(item1, items.get(0));
        Assert.assertEquals(item2, items.get(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addComponentAtIndex_negativeIndex() {
        addDivAtIndex(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void addComponentAtIndex_indexIsBiggerThanChildrenCount() {
        addDivAtIndex(1);
    }

    @Test
    public void addItemsWithNullClickListeners_doesNotThrow() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem foo = contextMenu.addItem("foo", null);
        contextMenu.addItem(new Div(), null);

        foo.getSubMenu().addItem("bar", null);
        foo.getSubMenu().addItem(new Div(), null);
    }

    @Test
    public void serializeContextMenu() throws IOException {
        NativeButton menuButton = new NativeButton();
        ContextMenu menu = new ContextMenu(menuButton);
        menu.add(new Label());
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
        Assert.assertTrue(
                "Component is not an instance of " + MenuItem.class.getName(),
                MenuItem.class.isInstance(component));
        Assert.assertEquals(expectedText, ((MenuItem) component).getText());
    }

}
