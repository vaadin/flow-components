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
package com.vaadin.flow.component.contextmenu;

import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;

/**
 * Unit tests for the ContextMenu.
 */
public class ContextMenuTest {

    @Test
    public void createContextMenuWithComponents_componentsArePartOfGetChildren() {
        Label label1 = new Label("Label 1");
        Label label2 = new Label("Label 2");
        Label label3 = new Label("Label 3");

        ContextMenu contextMenu = new ContextMenu();
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

        label1.getElement().removeFromParent();
        children = contextMenu.getChildren().collect(Collectors.toList());
        Assert.assertEquals(1, children.size());
        Assert.assertThat(children, CoreMatchers.hasItems(label3));

        contextMenu.removeAll();
        children = contextMenu.getChildren().collect(Collectors.toList());
        Assert.assertEquals(0, children.size());
    }

    @Test
    public void addComponents_removeAll_getChildrenReturnsEmpty() {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.add(new Label("1"), new Label("2"));
        contextMenu.removeAll();
        Assert.assertEquals(
                "getChildren() should not return any components after calling removeAll()",
                0, contextMenu.getChildren().count());
    }

}
