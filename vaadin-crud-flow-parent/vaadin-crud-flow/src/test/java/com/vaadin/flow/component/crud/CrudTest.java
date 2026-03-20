/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.crud;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ObjectNode;

class CrudTest {

    final Crud<Thing> systemUnderTest = new Crud<>(Thing.class,
            createFakeGrid(), new ThingEditor());

    @Test
    void itemAvailableInAllEvents() {
        // Assert that all these events come with an item.
        systemUnderTest
                .addCancelListener(e -> Assertions.assertNotNull(e.getItem()));
        systemUnderTest
                .addDeleteListener(e -> Assertions.assertNotNull(e.getItem()));
        systemUnderTest
                .addEditListener(e -> Assertions.assertNotNull(e.getItem()));
        systemUnderTest
                .addSaveListener(e -> Assertions.assertNotNull(e.getItem()));
        systemUnderTest
                .addNewListener(e -> Assertions.assertNotNull(e.getItem()));

        // A client-side Grid item.
        final ObjectNode selectedItem = JacksonUtils.createObjectNode();
        selectedItem.put("key", "1");

        // Simulate a sequence of interactions.
        Arrays.asList(new Crud.NewEvent<>(systemUnderTest, false, null),
                new Crud.CancelEvent<>(systemUnderTest, false, null),

                new Crud.EditEvent<>(systemUnderTest, false, selectedItem,
                        null),
                new Crud.DeleteEvent<>(systemUnderTest, false, null),

                new Crud.EditEvent<>(systemUnderTest, false, selectedItem,
                        null),
                new Crud.SaveEvent<>(systemUnderTest, false, null))
                .forEach(e -> ComponentUtil.fireEvent(systemUnderTest, e));
    }

    @Test
    void newItemPreFilledValueIsTheSameInEditor() {
        String value = "thing";

        systemUnderTest.addNewListener(e -> {
            Thing item = e.getItem();
            item.name = value;
        });

        ComponentUtil.fireEvent(systemUnderTest,
                new Crud.NewEvent<>(systemUnderTest, false, null));

        Assertions.assertEquals("thing",
                systemUnderTest.getEditor().getItem().name);
    }

    @Test
    void crudEditorIsItemEqualNewEventItem() {
        systemUnderTest.addNewListener(e -> {
            Assertions.assertEquals(systemUnderTest.getEditor().getItem(),
                    e.getItem());
        });

        ComponentUtil.fireEvent(systemUnderTest,
                new Crud.NewEvent<>(systemUnderTest, false, null));
    }

    @Test
    void getEditorPosition_defaultOVERLAY() {
        Assertions.assertEquals(CrudEditorPosition.OVERLAY,
                systemUnderTest.getEditorPosition());
    }

    @Test
    void getToolbarVisible_defaultTrue() {
        Assertions.assertTrue(systemUnderTest.getToolbarVisible());
    }

    @Test
    void getToolbarVisible_setVisibleToFalse_returnsFalse() {
        systemUnderTest.setToolbarVisible(false);
        Assertions.assertEquals(false, systemUnderTest.getToolbarVisible());
    }

    @Test
    void crudHasStyle() {
        Assertions.assertTrue(systemUnderTest instanceof HasStyle);
    }

    private Grid<Thing> createFakeGrid() {
        Grid<Thing> grid = Mockito.spy(new Grid<>());

        Mockito.when(grid.getDataProvider())
                .thenReturn(Mockito.mock(DataProvider.class));

        DataCommunicator<Thing> communicator = Mockito
                .mock(DataCommunicator.class);
        Mockito.when(grid.getDataCommunicator()).thenReturn(communicator);

        DataKeyMapper<Thing> keyMapper = Mockito.mock(DataKeyMapper.class);
        Mockito.when(communicator.getKeyMapper()).thenReturn(keyMapper);

        Mockito.when(keyMapper.get("1")).thenReturn(new Thing());
        return grid;
    }

    public static class Thing {
        String name;
    }

    private static class ThingEditor implements CrudEditor<Thing> {
        private Thing item;

        @Override
        public void setItem(Thing item, boolean validate) {
            this.item = item;
        }

        @Override
        public Thing getItem() {
            return item;
        }

        @Override
        public void clear() {
        }

        @Override
        public boolean validate() {
            return false;
        }

        @Override
        public void writeItemChanges() {
        }

        @Override
        public Component getView() {
            return new Div();
        }
    }
}
