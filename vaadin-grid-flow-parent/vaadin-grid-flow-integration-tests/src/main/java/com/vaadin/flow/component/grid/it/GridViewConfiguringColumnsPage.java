/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.GridSelectionModel;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid-it-demo/configuring-columns")
public class GridViewConfiguringColumnsPage extends LegacyTestView {

    public GridViewConfiguringColumnsPage() {
        createColumnApiExample();
        createBeanGrid();
    }

    private void createColumnApiExample() {
        Grid<Person> grid = new Grid<>();
        GridSelectionModel<Person> selectionMode = grid
                .setSelectionMode(SelectionMode.MULTI);
        grid.setItems(getItems());

        Column<Person> idColumn = grid.addColumn(Person::getId).setHeader("ID")
                .setFlexGrow(0).setWidth("75px");

        grid.addColumn(Person::getFirstName).setHeader("Name")
                .setResizable(true);

        // Setting a column-key allows fetching the column later
        grid.addColumn(Person::getAge).setHeader("Age").setKey("age");
        Column<Person> ageColumn = grid.getColumnByKey("age");
        ageColumn.setResizable(true);

        NativeButton idColumnVisibility = new NativeButton(
                "Toggle visibility of the ID column");
        idColumnVisibility.addClickListener(
                event -> idColumn.setVisible(!idColumn.isVisible()));

        NativeButton userReordering = new NativeButton(
                "Toggle user reordering of columns");
        userReordering.addClickListener(event -> grid
                .setColumnReorderingAllowed(!grid.isColumnReorderingAllowed()));

        NativeButton freezeIdColumn = new NativeButton(
                "Toggle frozen state of ID column");
        freezeIdColumn.addClickListener(
                event -> idColumn.setFrozen(!idColumn.isFrozen()));

        NativeButton freezeSelectionColumn = new NativeButton(
                "Toggle frozen state of selection column");
        GridMultiSelectionModel<?> multiSlection = (GridMultiSelectionModel<?>) selectionMode;
        freezeSelectionColumn.addClickListener(
                event -> multiSlection.setSelectionColumnFrozen(
                        !multiSlection.isSelectionColumnFrozen()));

        NativeButton freezeAgeColumnToEnd = new NativeButton(
                "Toggle frozen to end state of Age column");
        freezeAgeColumnToEnd.addClickListener(
                event -> ageColumn.setFrozenToEnd(!ageColumn.isFrozenToEnd()));

        RadioButtonGroup<ColumnTextAlign> alignments = new RadioButtonGroup<>();
        alignments.setItems(ColumnTextAlign.values());
        alignments.setLabel("Text alignment for the Age column");
        alignments.setValue(ColumnTextAlign.START);
        alignments.addValueChangeListener(event -> grid.getColumnByKey("age")
                .setTextAlign(event.getValue()));

        grid.setId("column-api-example");
        idColumnVisibility.setId("toggle-id-column-visibility");
        userReordering.setId("toggle-user-reordering");
        freezeIdColumn.setId("toggle-id-column-frozen");
        freezeSelectionColumn.setId("toggle-selection-column-frozen");
        freezeAgeColumnToEnd.setId("toggle-age-column-frozen-to-end");
        alignments.setId("toggle-text-align");
        addCard("Configuring columns", "Column API example", grid,
                new VerticalLayout(idColumnVisibility, userReordering,
                        freezeIdColumn, freezeSelectionColumn,
                        freezeAgeColumnToEnd, alignments));
    }

    private void createBeanGrid() {
        // Providing a bean-type generates columns for all of it's properties
        Grid<Person> grid = new Grid<>(Person.class);
        grid.setItems(getItems());

        // Property-names are automatically set as keys
        // You can remove undesired columns by using the key
        grid.removeColumnByKey("id");

        // Columns for sub-properties can be added easily
        grid.addColumn("address.postalCode");

        // You can also configure the included properties and their order with
        // a single method call
        NativeButton showBasicInformation = new NativeButton(
                "Show basic information",
                event -> grid.setColumns("firstName", "age", "address"));
        NativeButton showAddressInformation = new NativeButton(
                "Show address information",
                event -> grid.setColumns("address.street", "address.number",
                        "address.postalCode"));
        grid.setId("bean-grid");
        showBasicInformation.setId("show-basic-information");
        showAddressInformation.setId("show-address-information");
        addCard("Configuring Columns", "Automatically adding columns", grid,
                showBasicInformation, showAddressInformation);
    }
}
