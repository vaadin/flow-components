package com.vaadin.flow.component.gridpro;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.gridpro.GridPro.EditColumn;

public class GridProEditColumnTest {

    GridPro<String> grid;
    GridPro.EditColumn<String> textColumn;
    GridPro.EditColumn<String> checkboxColumn;
    GridPro.EditColumn<String> selectColumn;
    List<String> listOptions;
    ItemUpdater itemUpdater;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void init() {
        itemUpdater = (item, newValue) -> {
            Assert.assertNotNull(item);
            Assert.assertNotNull(newValue);
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
    public void setKey_getByKey() {
        // Grid columns API should be available from GridPro EditColumn
        textColumn.setKey("foo");
        checkboxColumn.setKey("bar");
        Assert.assertEquals(textColumn, grid.getColumnByKey("foo"));
        Assert.assertEquals(checkboxColumn, grid.getColumnByKey("bar"));
    }

    @Test
    public void removeColumnByKey() {
        textColumn.setKey("first");
        grid.removeColumnByKey("first");
        Assert.assertNull(grid.getColumnByKey("first"));
    }

    @Test
    public void removeColumn() {
        textColumn.setKey("first");
        grid.removeColumn(textColumn);
        Assert.assertNull(grid.getColumnByKey("first"));
    }

    @Test
    public void setItemUpdater_getItemUpdater() {
        ItemUpdater itemUpdater = (Object item, Object newValue) -> {
        };
        checkboxColumn.setItemUpdater(itemUpdater);
        Assert.assertEquals(checkboxColumn.getItemUpdater(), itemUpdater);
    }

    @Test
    public void setEditorType_getEditorType() {
        EditorType editorType = EditorType.CHECKBOX;
        textColumn.setEditorType(editorType);
        Assert.assertEquals(textColumn.getEditorType(),
                editorType.getTypeName());
    }

    @Test
    public void setOptions_getOptions() {
        selectColumn.setOptions(listOptions);
        Assert.assertEquals(selectColumn.getOptions(), listOptions);
    }

    @Test
    public void addColumn_changeEditorType() {
        GridPro<Person> grid = new GridPro<>();

        GridPro.EditColumn<Person> nameColumn = (EditColumn<Person>) grid
                .addEditColumn(Person::getName).text((item, newValue) -> {
                });
        nameColumn.setEditorType(EditorType.CHECKBOX);
        Assert.assertEquals(nameColumn.getEditorType(),
                EditorType.CHECKBOX.getTypeName());

        nameColumn.setEditorType(EditorType.SELECT);
        Assert.assertEquals(nameColumn.getEditorType(),
                EditorType.SELECT.getTypeName());

        nameColumn.setEditorType(EditorType.TEXT);
        Assert.assertEquals(nameColumn.getEditorType(),
                EditorType.TEXT.getTypeName());
    }

    @Test
    public void addEditColumn_returnsNonNullAndEditColumnType() {
        Grid.Column<Person> column = new GridPro<Person>()
                .addEditColumn(str -> str).text((item, newValue) -> {
                });
        Assert.assertNotNull(column);
        Assert.assertEquals(GridPro.EditColumn.class, column.getClass());
    }

    @Test
    public void editCell_validateInvalidCalls() {
        grid.addColumn(str -> str).setHeader("Non editable col 1").setId("col-ne");                     // #4
        grid.addColumn(str -> str).setHeader("Non editable col 2");                                     // #5
        grid.addEditColumn(str -> str).text(itemUpdater).setHeader("Editable col 4");                   // #6
        grid.addColumn(str -> str).setHeader("Non editable col 2").setId("col-e");                      // #7
        grid.addEditColumn(str -> str).text(itemUpdater).setHeader("Editable col 5");                   // #8
        Grid.Column<String> invisibleCol = grid.addEditColumn(str -> str).text(itemUpdater)
                .setHeader("Editable col 6");                                                           // #9

        invisibleCol.setId("invisible1");
        invisibleCol.setVisible(false);

        grid.editCell(1,1);
        grid.editCell(2,3); // not editable. should pass because validation is not on java side
        try {
            grid.editCell(2, 8); // not visible (out of bounds then)
            Assert.fail();
        } catch (IndexOutOfBoundsException ex) {}
        try {
            grid.editCell(200,1); // out of bounds
            Assert.fail();
        } catch (IndexOutOfBoundsException ex) {}
        try {
            grid.editCell(2,300); // out of bounds
            Assert.fail();
        } catch (IndexOutOfBoundsException ex) {}
        grid.editCell(2, "col-ne"); // not editable. should pass because validation is not on java side
        grid.editCell(2, "col-e");
        try {
            grid.editCell(2, "does-not-exist"); // not found
            Assert.fail();
        } catch (IllegalArgumentException ex) {}
        try {
            grid.editCell(2, "invisible"); // not found because it's invisible
            Assert.fail();
        } catch (IllegalArgumentException ex) {}
        grid.editCell("foo", 1);
        grid.editCell("foo", 3); // not editable. should pass because validation is not on java side
        try {
            grid.editCell("foo", 300); // out of bounds
            Assert.fail();
        } catch (IndexOutOfBoundsException ex) {}
        grid.editCell("does-not-exist", 1); // not found. it will pass because validation only occurs on client side
        grid.editCell("bar", "col-ne"); // not editable. should pass because validation is not on java side
        grid.editCell("bar", "col-e");
        try {
            grid.editCell("does not exist", "does-not-exist"); // not found
            Assert.fail();
        } catch (IllegalArgumentException ex) {}
    }
}
