/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/editor")
public class GridEditorPage extends Div {

    private final Binder<Person> binder = new Binder<>(Person.class);

    public GridEditorPage() {
        Grid<Person> grid = new Grid<>();

        final List<Person> items = createGridData();
        grid.setItems(items);

        Editor<Person> editor = grid.getEditor();
        editor.setBinder(binder);

        createColumnWithEditor(grid);

        NativeButton subsequentEditRequests = new NativeButton(
                "Subsequent edits", event -> {
                    editor.editItem(items.get(0));
                    editor.editItem(items.get(1));
                });
        subsequentEditRequests.setId("subsequent-edit-requests");

        NativeButton add100Items = new NativeButton("Add 100 items", event -> {
            for (int i = 0; i < 100; i++) {
                items.add(new Person("foo" + i, 10 + i));
            }
            grid.getDataProvider().refreshAll();

        });
        add100Items.setId("add-100-items");

        NativeButton editLastItem = new NativeButton("Edit last item",
                event -> {
                    editor.editItem(items.get(items.size() - 1));
                });
        editLastItem.setId("edit-last-item");

        add(grid, subsequentEditRequests, add100Items, editLastItem);
    }

    private void createColumnWithEditor(Grid<Person> grid) {
        Grid.Column<Person> nameColumn = grid.addColumn(Person::getFirstName)
                .setHeader("Name");

        TextField field = new TextField();
        binder.bind(field, "firstName");
        nameColumn.setEditorComponent(field);
    }

    private List<Person> createGridData() {
        List<Person> items = new ArrayList<>();
        items.add(new Person("foo", 10));
        items.add(new Person("bar", 11));
        items.add(new Person("yxz", 12));
        return items;
    }
}
