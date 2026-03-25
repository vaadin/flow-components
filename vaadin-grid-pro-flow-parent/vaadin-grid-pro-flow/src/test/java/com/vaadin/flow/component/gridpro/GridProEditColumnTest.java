/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.gridpro;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.gridpro.GridPro.EditColumn;
import com.vaadin.flow.shared.Registration;

class GridProEditColumnTest {

    GridPro<String> grid;
    GridPro.EditColumn<String> textColumn;
    GridPro.EditColumn<String> checkboxColumn;
    GridPro.EditColumn<String> selectColumn;
    List<String> listOptions;
    ItemUpdater itemUpdater;

    @BeforeEach
    void setup() {
        itemUpdater = (item, newValue) -> {
            Assertions.assertNotNull(item);
            Assertions.assertNotNull(newValue);
        };

        grid = new GridPro<>();
        textColumn = (EditColumn<String>) grid.addEditColumn(str -> str)
                .text(itemUpdater);
        checkboxColumn = (EditColumn<String>) grid.addEditColumn(str -> str)
                .checkbox(itemUpdater);

        listOptions = new ArrayList<>();
        listOptions.add("foo");
        listOptions.add("bar");
        listOptions.add("baz");
        selectColumn = (EditColumn<String>) grid.addEditColumn(str -> str)
                .select(itemUpdater, listOptions);
    }

    @Test
    void setKey_getByKey() {
        // Grid columns API should be available from GridPro EditColumn
        textColumn.setKey("foo");
        checkboxColumn.setKey("bar");
        Assertions.assertEquals(textColumn, grid.getColumnByKey("foo"));
        Assertions.assertEquals(checkboxColumn, grid.getColumnByKey("bar"));
    }

    @Test
    void removeColumnByKey() {
        textColumn.setKey("first");
        grid.removeColumnByKey("first");
        Assertions.assertNull(grid.getColumnByKey("first"));
    }

    @Test
    void removeColumn() {
        textColumn.setKey("first");
        grid.removeColumn(textColumn);
        Assertions.assertNull(grid.getColumnByKey("first"));
    }

    @Test
    void setItemUpdater_getItemUpdater() {
        ItemUpdater itemUpdater = (Object item, Object newValue) -> {
        };
        checkboxColumn.setItemUpdater(itemUpdater);
        Assertions.assertEquals(checkboxColumn.getItemUpdater(), itemUpdater);
    }

    @Test
    void setEditorType_getEditorType() {
        EditorType editorType = EditorType.CHECKBOX;
        textColumn.setEditorType(editorType);
        Assertions.assertEquals(textColumn.getEditorType(),
                editorType.getTypeName());
    }

    @Test
    void setOptions_getOptions() {
        selectColumn.setOptions(listOptions);
        Assertions.assertEquals(selectColumn.getOptions(), listOptions);
    }

    @Test
    void addColumn_changeEditorType() {
        GridPro<Person> grid = new GridPro<>();

        GridPro.EditColumn<Person> nameColumn = (EditColumn<Person>) grid
                .addEditColumn(Person::getName).text((item, newValue) -> {
                });
        nameColumn.setEditorType(EditorType.CHECKBOX);
        Assertions.assertEquals(nameColumn.getEditorType(),
                EditorType.CHECKBOX.getTypeName());

        nameColumn.setEditorType(EditorType.SELECT);
        Assertions.assertEquals(nameColumn.getEditorType(),
                EditorType.SELECT.getTypeName());

        nameColumn.setEditorType(EditorType.TEXT);
        Assertions.assertEquals(nameColumn.getEditorType(),
                EditorType.TEXT.getTypeName());
    }

    @Test
    void addEditColumn_returnsNonNullAndEditColumnType() {
        Grid.Column<Person> column = new GridPro<Person>()
                .addEditColumn(str -> str).text((item, newValue) -> {
                });
        Assertions.assertNotNull(column);
        Assertions.assertEquals(GridPro.EditColumn.class, column.getClass());
    }

    @SuppressWarnings("unchecked")
    @Test
    void addEditColumn_acceptsImplementationOfHasElementAndValue() {
        GridPro<Person> gridPro = new GridPro<>();
        gridPro.addEditColumn(Person::getName).custom(new TestCustomEditor(),
                (person, value) -> {
                });
    }

    @Tag("test-custom-editor")
    private static class TestCustomEditor extends Component implements
            HasValueAndElement<HasValue.ValueChangeEvent<String>, String> {
        @Override
        public void setValue(String value) {

        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        public Registration addValueChangeListener(
                ValueChangeListener<? super ValueChangeEvent<String>> listener) {
            return null;
        }
    }
}
