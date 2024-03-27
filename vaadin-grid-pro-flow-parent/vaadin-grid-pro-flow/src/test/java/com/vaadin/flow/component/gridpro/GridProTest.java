package com.vaadin.flow.component.gridpro;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.data.provider.DataProvider;
import elemental.json.JsonObject;
import elemental.json.impl.JreJsonFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;

public class GridProTest {

    GridPro<Person> grid;
    JsonObject selectedItem;
    ArrayList<Person> items = new ArrayList<>();
    Person testItem = new Person("Foo", 1996);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        grid = createFakeGridPro();

        // We should ensure the correct value were passed
        grid.addEditColumn(Person::getName)
                .text((item, newValue) -> Assert.assertEquals("foo", newValue));

        // A client-side Grid item.
        selectedItem = new JreJsonFactory()
                .parse("{\"key\": \"1\", \"col0\":\"foo\"}");
    }

    private GridPro<Person> createFakeGridPro() {
        GridPro<Person> grid = Mockito.spy(GridPro.class);

        Mockito.when(grid.getDataProvider())
                .thenReturn(Mockito.mock(DataProvider.class));

        DataCommunicator<Person> communicator = Mockito
                .mock(DataCommunicator.class);
        Mockito.when(grid.getDataCommunicator()).thenReturn(communicator);

        KeyMapper<Person> keyMapper = Mockito.mock(KeyMapper.class);
        Mockito.when(communicator.getKeyMapper()).thenReturn(keyMapper);

        Mockito.when(keyMapper.get("1")).thenReturn(testItem);
        return grid;
    }

    @Test
    public void setEnterNextRow_getEnterNextRow() {
        grid.setEnterNextRow(true);
        Assert.assertEquals(grid.getEnterNextRow(), true);
    }

    @Test
    public void setSingleCellEdit_getSingleCellEdit() {
        grid.setSingleCellEdit(true);
        Assert.assertTrue(grid.getSingleCellEdit());
    }

    @Test
    public void setEditOnClick_getEditOnClick() {
        grid.setEditOnClick(true);
        Assert.assertTrue(grid.getEditOnClick());
    }

    @Test
    public void itemAvailableInAllEvents() {
        // Assert that all events come with an item.
        grid.addCellEditStartedListener(e -> items.add(e.getItem()));
        grid.addItemPropertyChangedListener(e -> items.add(e.getItem()));

        // Simulate a sequence of interactions.
        Arrays.asList(
                new GridPro.CellEditStartedEvent<>(grid, false, selectedItem,
                        "col0"),
                new GridPro.ItemPropertyChangedEvent<>(grid, false,
                        selectedItem, "col0"))
                .forEach(e -> ComponentUtil.fireEvent(grid, e));

        Assert.assertEquals(2, items.size());
        items.forEach(item -> Assert.assertEquals(testItem, item));
    }

    @Test
    public void propertyChangedEvent_itemNotPresentDataProvider_itemUpdaterNotCalled() {
        var dataProvider = grid.getDataProvider();
        Mockito.doNothing().when(dataProvider).refreshItem(Mockito.isNull());

        ItemUpdater<Person, String> mock = Mockito.mock(ItemUpdater.class);
        grid.addEditColumn(Person::getName).text(mock);

        JsonObject item = new JreJsonFactory()
                .parse("{\"key\": \"2\", \"col1\":\"foo\"}");

        ComponentUtil.fireEvent(grid,
                new GridPro.ItemPropertyChangedEvent<Person>(grid, false, item,
                        "col1"));
        Mockito.verify(mock, Mockito.never()).accept(Mockito.isNull(),
                Mockito.anyString());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void itemPropertyChangedListener_onlyCalledWhenCellIsEditable() {
        ComponentEventListener listener = Mockito
                .mock(ComponentEventListener.class);
        grid.addItemPropertyChangedListener(listener);

        ItemUpdater<Person, String> itemUpdater = Mockito
                .mock(ItemUpdater.class);
        GridPro.EditColumn<Person> column = (GridPro.EditColumn<Person>) grid
                .addEditColumn(Person::getName).text(itemUpdater);
        JsonObject item = new JreJsonFactory()
                .parse("{\"key\": \"1\", \"col1\":\"foo\"}");
        GridPro.ItemPropertyChangedEvent<Person> event = new GridPro.ItemPropertyChangedEvent<>(
                grid, true, item, "col1");

        // No editable provider - should fire event
        ComponentUtil.fireEvent(grid, event);
        Mockito.verify(listener, Mockito.times(1))
                .onComponentEvent(Mockito.any());
        Mockito.verify(itemUpdater, Mockito.times(1)).accept(Mockito.any(),
                Mockito.any());

        // Cell is not editable - should not fire event
        column.setCellEditableProvider(person -> false);
        Mockito.reset(listener, itemUpdater);
        ComponentUtil.fireEvent(grid, event);
        Mockito.verify(listener, Mockito.never())
                .onComponentEvent(Mockito.any());
        Mockito.verify(itemUpdater, Mockito.never()).accept(Mockito.any(),
                Mockito.any());

        // Cell is editable - should fire event
        column.setCellEditableProvider(person -> true);
        Mockito.reset(listener, itemUpdater);
        ComponentUtil.fireEvent(grid, event);
        Mockito.verify(listener, Mockito.times(1))
                .onComponentEvent(Mockito.any());
        Mockito.verify(itemUpdater, Mockito.times(1)).accept(Mockito.any(),
                Mockito.any());
    }
}
