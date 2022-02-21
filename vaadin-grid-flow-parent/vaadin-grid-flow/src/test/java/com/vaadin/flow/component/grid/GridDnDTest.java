package com.vaadin.flow.component.grid;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dnd.DropEvent;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.dnd.EffectAllowed;
import com.vaadin.flow.component.grid.dnd.GridDragEndEvent;
import com.vaadin.flow.component.grid.dnd.GridDragStartEvent;
import com.vaadin.flow.router.RouterLink;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

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
        JsonObject object = Json.createObject();
        JsonArray array = Json.createArray();
        JsonObject item = Json.createObject();
        item.put("key", grid.getDataCommunicator().getKeyMapper().key("2"));
        array.set(0, item);
        object.put("draggedItems", array);

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
