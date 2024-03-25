/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

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
        addCard("Header and footer rows", "Adding header and footer rows",
                grid);
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
