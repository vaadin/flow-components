/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.popover;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.server.VaadinSession;

import elemental.json.JsonArray;
import elemental.json.impl.JsonUtil;

public class PopoverChildrenTest {

    private UI ui = new UI();
    private Popover popover;

    @Before
    public void setup() {
        UI.setCurrent(ui);

        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);

        popover = new Popover();
        ui.add(popover);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void add_virtualNodeIdsInSync() {
        var child = new Div();
        popover.add(child);
        assertVirtualChildren(child);
    }

    @Test
    public void addMany_virtualNodeIdsInSync() {
        var child = new Div();
        var child2 = new Div();
        popover.add(child);
        popover.add(child2);
        assertVirtualChildren(child, child2);
    }

    @Test
    public void addCollection_virtualNodeIdsInSync() {
        var child = new Div();
        var child2 = new Div();
        popover.add(List.of(child, child2));
        assertVirtualChildren(child, child2);
    }

    @Test
    public void addComponentAsFirst_virtualNodeIdsInSync() {
        var child = new Div();
        var child2 = new Div();
        popover.add(child);
        popover.addComponentAsFirst(child2);
        assertVirtualChildren(child2, child);
    }

    @Test
    public void remove_virtualNodeIdsInSync() {
        var child = new Div();
        var child2 = new Div();
        popover.add(child, child2);
        popover.remove(child2);
        assertVirtualChildren(child);
    }

    @Test
    public void removeAll_virtualNodeIdsInSync() {
        var child = new Div();
        var child2 = new Div();
        popover.add(child, child2);
        popover.removeAll();
        assertVirtualChildren();
    }

    @Test
    public void addComponentAtIndex_virtualNodeIdsInSync() {
        var child = new Div();
        var child2 = new Div();
        var child3 = new Div();
        popover.add(child, child2);
        popover.addComponentAtIndex(1, child3);
        assertVirtualChildren(child, child3, child2);
    }

    @Test
    public void addTextNodes_virtualNodeIdsInSync() {
        var child = new Text("text");
        var child2 = new Text("text2");
        popover.add(child, child2);
        assertVirtualChildren(child, child2);
    }

    @Test
    public void addBeforeAttaching_validNodeIds() {
        popover = new Popover();
        var child = new Div();
        popover.add(child);
        ui.add(popover);
        Assert.assertNotEquals("[-1]",
                popover.getElement().getProperty("virtualChildNodeIds"));
    }

    @Test
    public void selfRemoveChild_virtualNodeIdsInSync() {
        var child = new Div();
        var child2 = new Div();
        popover.add(child, child2);
        child.removeFromParent();
        assertVirtualChildren(child2);
    }

    @Test
    public void addSeparately_selfRemoveChild_doesNotThrow() {
        var child = new Div();
        var child2 = new Div();
        popover.add(child);
        popover.add(child2);
        child.removeFromParent();
        assertVirtualChildren(child2);
    }

    @Test
    public void relocateChild_detachListenerRemoved() {
        var child = new Div();
        popover.add(child);

        // Move the child to a new parent
        var newParent = new Div();
        ui.add(newParent);
        newParent.add(child);

        // Should be empty of virtual children
        assertVirtualChildren();
        // Manually modify the virtualChildNodeIds property
        popover.getElement().setProperty("virtualChildNodeIds", "[-1]");

        newParent.remove(child);
        Assert.assertEquals(
                popover.getElement().getProperty("virtualChildNodeIds"),
                "[-1]");
    }

    private void assertVirtualChildren(Component... components) {
        // Get a List of the node ids
        var childIds = Arrays.stream(components)
                .map(component -> component.getElement().getNode().getId())
                .collect(Collectors.toList());

        // Get the virtualChildNodeIds property from the popover as a JsonArray
        var jsonArrayOfIds = (JsonArray) JsonUtil
                .parse(popover.getElement().getProperty("virtualChildNodeIds"));

        var virtualChildNodeIds = JsonUtils.numberStream(jsonArrayOfIds)
                .mapToInt(i -> (int) i).boxed().collect(Collectors.toList());

        Assert.assertEquals(childIds, virtualChildNodeIds);
    }

}
