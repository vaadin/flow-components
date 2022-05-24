package com.vaadin.flow.component.gridpro;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.shared.Registration;
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
    public void init() {
        grid = createFakeGridPro();

        // We should ensure the correct value were passed
        grid.addEditColumn(Person::getName)
                .text((item, newValue) -> Assert.assertEquals("foo", newValue));

        // A client-side Grid item.
        selectedItem = new JreJsonFactory()
                .parse("{\"key\": \"1\", \"col0\":\"foo\"}");
    }

    private GridPro<Person> createFakeGridPro() {
        GridPro<Person> grid = Mockito.spy(new GridPro<>());

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
    public void addCustomColumn_hasValueAndElement() {
        FakeField field = new FakeField();
        grid.addEditColumn(Person::getName).custom(field,
                (item, newValue) -> item.setName(newValue));
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

    @Tag(Tag.DIV)
    public class FakeField extends Component implements
            HasValueAndElement<ComponentValueChangeEvent<FakeField, String>, String> {
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
        @SuppressWarnings("unchecked")
        public Registration addValueChangeListener(
                ValueChangeListener<? super ComponentValueChangeEvent<FakeField, String>> listener) {
            @SuppressWarnings("rawtypes")
            ComponentEventListener componentListener = event -> {
                ComponentValueChangeEvent<FakeField, String> valueChangeEvent = (ComponentValueChangeEvent<FakeField, String>) event;
                listener.valueChanged(valueChangeEvent);
            };
            return ComponentUtil.addListener(this,
                    ComponentValueChangeEvent.class, componentListener);
        }
    }
}
