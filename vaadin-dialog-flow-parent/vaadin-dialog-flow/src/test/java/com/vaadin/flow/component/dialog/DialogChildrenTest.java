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
package com.vaadin.flow.component.dialog;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

class DialogChildrenTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Dialog dialog;

    @BeforeEach
    void setup() {
        dialog = new Dialog();
        ui.add(dialog);
    }

    @Test
    void add_virtualNodeIdsInSync() {
        var child = new Div();
        dialog.add(child);
        assertVirtualChildren(child);
    }

    @Test
    void addMany_virtualNodeIdsInSync() {
        var child = new Div();
        var child2 = new Div();
        dialog.add(child);
        dialog.add(child2);
        assertVirtualChildren(child, child2);
    }

    @Test
    void addCollection_virtualNodeIdsInSync() {
        var child = new Div();
        var child2 = new Div();
        dialog.add(List.of(child, child2));
        assertVirtualChildren(child, child2);
    }

    @Test
    void addComponentAsFirst_virtualNodeIdsInSync() {
        var child = new Div();
        var child2 = new Div();
        dialog.add(child);
        dialog.addComponentAsFirst(child2);
        assertVirtualChildren(child2, child);
    }

    @Test
    void remove_virtualNodeIdsInSync() {
        var child = new Div();
        var child2 = new Div();
        dialog.add(child, child2);
        dialog.remove(child2);
        assertVirtualChildren(child);
    }

    @Test
    void removeAll_virtualNodeIdsInSync() {
        var child = new Div();
        var child2 = new Div();
        dialog.add(child, child2);
        dialog.removeAll();
        assertVirtualChildren();
    }

    @Test
    void addComponentAtIndex_virtualNodeIdsInSync() {
        var child = new Div();
        var child2 = new Div();
        var child3 = new Div();
        dialog.add(child, child2);
        dialog.addComponentAtIndex(1, child3);
        assertVirtualChildren(child, child3, child2);
    }

    @Test
    void addTextNodes_virtualNodeIdsInSync() {
        var child = new Text("text");
        var child2 = new Text("text2");
        dialog.add(child, child2);
        assertVirtualChildren(child, child2);
    }

    @Test
    void addBeforeAttaching_validNodeIds() {
        dialog = new Dialog();
        var child = new Div();
        dialog.add(child);
        ui.add(dialog);
        Assertions.assertNotEquals("[-1]",
                dialog.getElement().getProperty("virtualChildNodeIds"));
    }

    @Test
    void selfRemoveChild_virtualNodeIdsInSync() {
        var child = new Div();
        var child2 = new Div();
        dialog.add(child, child2);
        child.removeFromParent();
        assertVirtualChildren(child2);
    }

    @Test
    void addSeparately_selfRemoveChild_doesNotThrow() {
        var child = new Div();
        var child2 = new Div();
        dialog.add(child);
        dialog.add(child2);
        child.removeFromParent();
        assertVirtualChildren(child2);
    }

    @Test
    void relocateChild_detachListenerRemoved() {
        var child = new Div();
        dialog.add(child);

        // Move the child to a new parent
        var newParent = new Div();
        ui.add(newParent);
        newParent.add(child);

        // Should be empty of virtual children
        assertVirtualChildren();
        // Manually modify the virtualChildNodeIds property
        dialog.getElement().setProperty("virtualChildNodeIds", "[-1]");

        newParent.remove(child);
        Assertions.assertEquals("[-1]",
                dialog.getElement().getProperty("virtualChildNodeIds"));
    }

    @Test
    void headerAndFooterAddedAsVirtualChildren() {
        dialog.getHeader().add(new Div());
        dialog.getFooter().add(new Div());

        Assertions.assertEquals(0, dialog.getElement().getChildCount());
        Assertions.assertEquals(dialog.getElement(),
                dialog.getHeader().root.getParent());
        Assertions.assertEquals(dialog.getElement(),
                dialog.getFooter().root.getParent());
    }

    @Test
    void removeAll_doesNotRemoveHeaderOrFooter() {
        dialog.getHeader().add(new Div());
        dialog.getFooter().add(new Div());

        dialog.removeAll();

        Assertions.assertEquals(dialog.getElement(),
                dialog.getHeader().root.getParent());
        Assertions.assertEquals(dialog.getElement(),
                dialog.getFooter().root.getParent());
    }

    private void assertVirtualChildren(Component... components) {
        // Get a List of the node ids
        var childIds = Arrays.stream(components)
                .map(component -> component.getElement().getNode().getId())
                .collect(Collectors.toList());

        // Get the virtualChildNodeIds property from the dialog as a JsonArray
        var jsonArrayOfIds = (ArrayNode) JacksonUtils.getMapper().readTree(
                dialog.getElement().getProperty("virtualChildNodeIds"));

        var virtualChildNodeIds = JacksonUtils.stream(jsonArrayOfIds)
                .mapToInt(JsonNode::asInt).boxed().collect(Collectors.toList());

        Assertions.assertEquals(childIds, virtualChildNodeIds);
    }

}
