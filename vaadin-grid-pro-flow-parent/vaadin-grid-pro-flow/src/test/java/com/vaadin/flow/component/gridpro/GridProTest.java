/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.gridpro;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonObject;
import elemental.json.impl.JreJsonFactory;

public class GridProTest {

    GridPro<Person> grid;
    JsonObject selectedItem;
    ArrayList<Person> items = new ArrayList<>();
    Person testItem = new Person("Foo", 1996);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        VaadinSession session = Mockito.mock(VaadinSession.class);
        var ui = new UI();
        ui.getInternals().setSession(session);

        UI.setCurrent(ui);

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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void itemPropertyChangedListener_notCalledWhenValueUnchanged_checkbox() {
        ComponentEventListener listener = Mockito
                .mock(ComponentEventListener.class);
        grid.addItemPropertyChangedListener(listener);

        ItemUpdater<Person, Boolean> itemUpdater = Mockito
                .mock(ItemUpdater.class);
        grid.addEditColumn(p -> true).checkbox(itemUpdater);

        // Create item with checkbox value true
        JsonObject item = new JreJsonFactory()
                .parse("{\"key\": \"1\", \"col1\":true}");

        // Fire CellEditStartedEvent to store the pre-edit value
        ComponentUtil.fireEvent(grid,
                new GridPro.CellEditStartedEvent<>(grid, true, item, "col1"));

        // Fire ItemPropertyChangedEvent with the same value true
        ComponentUtil.fireEvent(grid, new GridPro.ItemPropertyChangedEvent<>(
                grid, true, item, "col1"));

        // Listener should NOT be called since value hasn't changed
        Mockito.verify(listener, Mockito.never())
                .onComponentEvent(Mockito.any());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void itemPropertyChangedListener_calledWhenValueChanged_checkbox() {
        ComponentEventListener listener = Mockito
                .mock(ComponentEventListener.class);
        grid.addItemPropertyChangedListener(listener);

        ItemUpdater<Person, Boolean> itemUpdater = Mockito
                .mock(ItemUpdater.class);
        grid.addEditColumn(p -> true).checkbox(itemUpdater);

        // Create item with initial checkbox value true
        JsonObject startItem = new JreJsonFactory()
                .parse("{\"key\": \"1\", \"col1\":true}");

        // Fire CellEditStartedEvent to store the pre-edit value
        ComponentUtil.fireEvent(grid, new GridPro.CellEditStartedEvent<>(grid,
                true, startItem, "col1"));

        // Create item with changed value false
        JsonObject changedItem = new JreJsonFactory()
                .parse("{\"key\": \"1\", \"col1\":false}");

        // Fire ItemPropertyChangedEvent with different value false
        ComponentUtil.fireEvent(grid, new GridPro.ItemPropertyChangedEvent<>(
                grid, true, changedItem, "col1"));

        // Listener SHOULD be called since value has changed
        Mockito.verify(listener, Mockito.times(1))
                .onComponentEvent(Mockito.any());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void itemPropertyChangedListener_notCalledWhenValueUnchanged_customEditor() {
        ComponentEventListener listener = Mockito
                .mock(ComponentEventListener.class);
        grid.addItemPropertyChangedListener(listener);

        ItemUpdater<Person, String> itemUpdater = Mockito
                .mock(ItemUpdater.class);
        TestCustomEditor customEditor = new TestCustomEditor();
        grid.addEditColumn(Person::getName).custom(customEditor, itemUpdater);

        // Create item
        JsonObject item = new JreJsonFactory()
                .parse("{\"key\": \"1\", \"col1\":\"Foo\"}");
        // Fire CellEditStartedEvent to store the pre-edit value
        ComponentUtil.fireEvent(grid,
                new GridPro.CellEditStartedEvent<>(grid, true, item, "col1"));

        // Simulate that custom editor still has the same value "Foo"
        customEditor.setValue("Foo");

        // Fire ItemPropertyChangedEvent
        ComponentUtil.fireEvent(grid, new GridPro.ItemPropertyChangedEvent<>(
                grid, true, item, "col1"));

        // Listener should NOT be called since custom editor value hasn't
        // changed
        Mockito.verify(listener, Mockito.never())
                .onComponentEvent(Mockito.any());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void itemPropertyChangedListener_calledWhenValueChanged_customEditor() {
        ComponentEventListener listener = Mockito
                .mock(ComponentEventListener.class);
        grid.addItemPropertyChangedListener(listener);

        ItemUpdater<Person, String> itemUpdater = Mockito
                .mock(ItemUpdater.class);

        TestCustomEditor customEditor = new TestCustomEditor();
        grid.addEditColumn(Person::getName).custom(customEditor, itemUpdater);

        // Create item
        JsonObject item = new JreJsonFactory()
                .parse("{\"key\": \"1\", \"col1\":\"Foo\"}");

        // Fire CellEditStartedEvent to store the pre-edit value
        ComponentUtil.fireEvent(grid,
                new GridPro.CellEditStartedEvent<>(grid, true, item, "col1"));

        // Set custom editor to "Bar"
        customEditor.setValue("Bar");

        ComponentUtil.fireEvent(grid, new GridPro.ItemPropertyChangedEvent<>(
                grid, true, item, "col1"));

        // Listener SHOULD be called since value changed from "Foo" (pre-edit)
        // to "Bar" (post-edit)
        Mockito.verify(listener, Mockito.times(1))
                .onComponentEvent(Mockito.any());
    }

    @Tag("test-custom-editor")
    private static class TestCustomEditor extends Component implements
            HasValueAndElement<HasValue.ValueChangeEvent<String>, String> {
        private String value;

        @Override
        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public Registration addValueChangeListener(
                HasValue.ValueChangeListener<? super HasValue.ValueChangeEvent<String>> listener) {
            return null;
        }
    }
}
