/*
 * Copyright 2000-2024 Vaadin Ltd.
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

        add(grid, subsequentEditRequests);
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
