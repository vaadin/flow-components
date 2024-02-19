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

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

import java.util.concurrent.atomic.AtomicInteger;

@Route("vaadin-grid-it-demo/header-and-footer-rows")
public class GridViewHeaderAndFooterRowsPage extends LegacyTestView {

    public GridViewHeaderAndFooterRowsPage() {
        createGridWithHeaderAndFooterRows();
        createHeaderAndFooterUsingComponents();
    }

    private void createGridWithHeaderAndFooterRows() {
        Grid<Person> grid = new Grid<>();
        grid.setItems(createItems());

        Column<Person> nameColumn = grid.addColumn(Person::getFirstName)
                .setHeader("Name").setComparator((p1, p2) -> p1.getFirstName()
                        .compareToIgnoreCase(p2.getFirstName()));
        Column<Person> ageColumn = grid.addColumn(Person::getAge, "age")
                .setHeader("Age");
        Column<Person> streetColumn = grid
                .addColumn(person -> person.getAddress().getStreet())
                .setHeader("Street");
        Column<Person> postalCodeColumn = grid
                .addColumn(person -> person.getAddress().getPostalCode())
                .setHeader("Postal Code");

        HeaderRow topRow = grid.prependHeaderRow();

        HeaderCell informationCell = topRow.join(nameColumn, ageColumn);
        informationCell.setText("Basic Information");

        HeaderCell addressCell = topRow.join(streetColumn, postalCodeColumn);
        addressCell.setText("Address Information");

        grid.appendFooterRow().getCell(nameColumn)
                .setText("Total: " + getItems().size() + " people");
        grid.setId("grid-with-header-and-footer-rows");

        RadioButtonGroup<Grid.SelectionMode> selectionMode = new RadioButtonGroup<>(
                "Selection mode");
        selectionMode.setItems(Grid.SelectionMode.values());
        selectionMode.setValue(grid.getSelectionMode());
        selectionMode.addValueChangeListener(
                event -> grid.setSelectionMode(event.getValue()));
        selectionMode.setItemLabelGenerator(item -> item.name().charAt(0)
                + item.name().substring(1).toLowerCase());
        selectionMode.setId("selection-mode");

        HorizontalLayout buttonsLayout = new HorizontalLayout();

        NativeButton removeAllHeaders = new NativeButton("Remove all headers",
                removeClick -> grid.removeAllHeaderRows());
        removeAllHeaders.setId("remove-all-headers");
        buttonsLayout.add(removeAllHeaders);

        AtomicInteger prependedHeaderIndex = new AtomicInteger();
        NativeButton prependHeader = new NativeButton("Prepend header",
                click -> {
                    String title = "Prepended header "
                            + prependedHeaderIndex.incrementAndGet();
                    HeaderRow headerRow = grid.prependHeaderRow();
                    headerRow.getCell(nameColumn).setText(title + " - 0");
                    headerRow.getCell(ageColumn).setText(title + " - 1");
                    headerRow.getCell(streetColumn).setText(title + " - 2");
                    headerRow.getCell(postalCodeColumn).setText(title + " - 3");
                });
        prependHeader.setId("prepend-header");
        buttonsLayout.add(prependHeader);

        AtomicInteger appendedHeaderIndex = new AtomicInteger();
        NativeButton appendHeader = new NativeButton("Append header", click -> {
            String title = "Appended header "
                    + appendedHeaderIndex.incrementAndGet();
            HeaderRow headerRow = grid.appendHeaderRow();
            headerRow.getCell(nameColumn).setText(title + " - 0");
            headerRow.getCell(ageColumn).setText(title + " - 1");
            headerRow.getCell(streetColumn).setText(title + " - 2");
            headerRow.getCell(postalCodeColumn).setText(title + " - 3");
        });
        appendHeader.setId("append-header");
        buttonsLayout.add(appendHeader);

        addCard("Header and footer rows", "Adding header and footer rows", grid,
                selectionMode, buttonsLayout);
    }

    private void createHeaderAndFooterUsingComponents() {
        Grid<Person> grid = new Grid<>();
        grid.setItems(getItems());

        Column<Person> nameColumn = grid.addColumn(Person::getFirstName)
                .setHeader(new Span("Name")).setComparator((p1, p2) -> p1
                        .getFirstName().compareToIgnoreCase(p2.getFirstName()));
        Column<Person> ageColumn = grid.addColumn(Person::getAge, "age")
                .setHeader(new Span("Age"));
        Column<Person> streetColumn = grid
                .addColumn(person -> person.getAddress().getStreet())
                .setHeader(new Span("Street"));
        Column<Person> postalCodeColumn = grid
                .addColumn(person -> person.getAddress().getPostalCode())
                .setHeader(new Span("Postal Code"));

        HeaderRow topRow = grid.prependHeaderRow();

        HeaderCell informationCell = topRow.join(nameColumn, ageColumn);
        informationCell.setComponent(new Span("Basic Information"));

        HeaderCell addressCell = topRow.join(streetColumn, postalCodeColumn);
        addressCell.setComponent(new Span("Address Information"));

        grid.appendFooterRow().getCell(nameColumn).setComponent(
                new Span("Total: " + getItems().size() + " people"));
        grid.setId("grid-header-with-components");
        addCard("Header and footer rows", "Header and footer using components",
                grid);
    }

}
