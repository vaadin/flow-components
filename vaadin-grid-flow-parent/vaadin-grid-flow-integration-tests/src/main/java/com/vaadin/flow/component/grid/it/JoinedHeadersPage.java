/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.Arrays;
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/joined-headers")
public class JoinedHeadersPage extends Div {

    public JoinedHeadersPage() {
        add(createGrid());
        add(createGrid2());
    }

    private static Grid<String> createGrid() {
        Grid<String> grid = new Grid<>();
        grid.setId("grid1");
        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        grid.appendHeaderRow();
        List<Grid.Column<String>> columns = addColumns(grid);
        HeaderRow.HeaderCell header = grid.prependHeaderRow()
                .join(columns.get(0), columns.get(1));
        header.setText("first + second");
        grid.prependHeaderRow()
                .join(columns.toArray(new Grid.Column[columns.size()]))
                .setText("joined");

        grid.setItems("1", "2", "3");
        return grid;
    }

    private static Grid<String> createGrid2() {
        Grid<String> grid = new Grid<>();
        grid.setId("grid2");
        addColumns(grid);
        grid.appendHeaderRow();
        HeaderRow header = grid.prependHeaderRow();
        header.join(header.getCells()).setText("Title");
        grid.setItems("1", "2", "3");
        return grid;
    }

    private static List<Grid.Column<String>> addColumns(Grid<String> grid) {
        Grid.Column<String> firstColumn = grid.addColumn(str -> str)
                .setKey("firstColumn").setHeader("First");
        Grid.Column<String> secondColumn = grid.addColumn(str -> str)
                .setKey("secondColumn").setHeader("Second");
        Grid.Column<String> thirdColumn = grid.addColumn(str -> str)
                .setKey("thirdColumn").setHeader("Third");
        Grid.Column<String> fourthColumn = grid.addComponentColumn(Span::new)
                .setKey("fourthColumn").setHeader("Forth");
        return Arrays.asList(firstColumn, secondColumn, thirdColumn,
                fourthColumn);
    }
}
