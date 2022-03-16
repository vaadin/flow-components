package com.vaadin.flow.component.crud;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.DataProvider;
import elemental.json.JsonObject;
import elemental.json.impl.JreJsonFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;

public class CrudTest {

    final Crud<Thing> systemUnderTest = new Crud<>(Thing.class,
            createFakeGrid(), new ThingEditor());

    @Test
    public void itemAvailableInAllEvents() {
        // Assert that all these events come with an item.
        systemUnderTest
                .addCancelListener(e -> Assert.assertNotNull(e.getItem()));
        systemUnderTest
                .addDeleteListener(e -> Assert.assertNotNull(e.getItem()));
        systemUnderTest.addEditListener(e -> Assert.assertNotNull(e.getItem()));
        systemUnderTest.addSaveListener(e -> Assert.assertNotNull(e.getItem()));
        systemUnderTest.addNewListener(e -> Assert.assertNotNull(e.getItem()));

        // A client-side Grid item.
        final JsonObject selectedItem = new JreJsonFactory()
                .parse("{\"key\": \"1\"}");

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
    public void newItemPreFilledValueIsTheSameInEditor() {
        String value = "thing";

        systemUnderTest.addNewListener(e -> {
            Thing item = e.getItem();
            item.name = value;
        });

        ComponentUtil.fireEvent(systemUnderTest,
                new Crud.NewEvent<>(systemUnderTest, false, null));

        Assert.assertEquals("thing",
                systemUnderTest.getEditor().getItem().name);
    }

    @Test
    public void crudEditorIsItemEqualNewEventItem() {
        systemUnderTest.addNewListener(e -> {
            Assert.assertEquals(systemUnderTest.getEditor().getItem(),
                    e.getItem());
        });

        ComponentUtil.fireEvent(systemUnderTest,
                new Crud.NewEvent<>(systemUnderTest, false, null));
    }

    @Test
    public void getEditorPosition_defaultOVERLAY() {
        Assert.assertEquals(CrudEditorPosition.OVERLAY,
                systemUnderTest.getEditorPosition());
    }

    @Test
    public void getToolbarVisible_defaultTrue() {
        Assert.assertTrue(systemUnderTest.getToolbarVisible());
    }

    @Test
    public void getToolbarVisible_setVisibleToFalse_returnsFalse() {
        systemUnderTest.setToolbarVisible(false);
        Assert.assertEquals(false, systemUnderTest.getToolbarVisible());
    }

    @Test
    public void crudHasStyle() {
        Assert.assertTrue(systemUnderTest instanceof HasStyle);
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
