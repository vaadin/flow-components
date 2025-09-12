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
package com.vaadin.flow.component.grid;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dnd.DropEvent;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.dnd.EffectAllowed;
import com.vaadin.flow.component.grid.dnd.GridDragEndEvent;
import com.vaadin.flow.component.grid.dnd.GridDragStartEvent;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.router.RouterLink;

public class GridDnDTest {

    private Grid<String> grid;
    private UI ui;

    @Before
    public void setup() {
        ui = new UI();

        grid = new Grid<String>() {
            @Override
            public Optional<UI> getUI() {
                return Optional.of(ui);
            }
        };
    }

    @Test
    public void gridDnD_genericDnD_activeDragComponentAndDataSet() {
        List<String> dragData = Collections.singletonList("2");
        ObjectNode object = JacksonUtils.createObjectNode();
        ArrayNode array = JacksonUtils.createArrayNode();
        ObjectNode item = JacksonUtils.createObjectNode();
        item.put("key", grid.getDataCommunicator().getKeyMapper().key("2"));
        array.add(item);
        object.set("draggedItems", array);

        GridDragStartEvent<String> startEvent = new GridDragStartEvent<String>(
                grid, true, object);
        ComponentUtil.fireEvent(grid, startEvent);

        Assert.assertEquals("No active drag source set", grid,
                ui.getActiveDragSourceComponent());
        Assert.assertEquals("No drag data set", dragData,
                ComponentUtil.getData(grid, Grid.DRAG_SOURCE_DATA_KEY));

        AtomicReference<DropEvent<RouterLink>> eventCapture = new AtomicReference<>();
        RouterLink routerLink = new RouterLink() {
            @Override
            public Optional<UI> getUI() {
                return Optional.of(ui);
            }
        };

        DropTarget.create(routerLink).addDropListener(eventCapture::set);

        ComponentUtil.fireEvent(routerLink, new DropEvent<RouterLink>(
                routerLink, true, EffectAllowed.ALL.getClientPropertyValue()));

        DropEvent<RouterLink> dropEvent = eventCapture.get();

        Assert.assertEquals("Incorrect drag data", dragData,
                dropEvent.getDragData().get());
        Assert.assertEquals("Incorrect drag source", grid,
                dropEvent.getDragSourceComponent().get());

        ComponentUtil.fireEvent(grid, new GridDragEndEvent<>(grid, true));

        Assert.assertNull("Active drag source not cleared",
                ui.getActiveDragSourceComponent());
        Assert.assertNull("Drag data not cleared",
                ComponentUtil.getData(grid, Grid.DRAG_SOURCE_DATA_KEY));
    }

}
