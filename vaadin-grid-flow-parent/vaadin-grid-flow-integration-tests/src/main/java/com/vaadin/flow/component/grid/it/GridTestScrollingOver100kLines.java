/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.time.LocalDate;
import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@Route("vaadin-grid/scroll-over-100k")
@Theme(Lumo.class)
public class GridTestScrollingOver100kLines extends Div {

    public GridTestScrollingOver100kLines() {
        setSizeFull();

        Grid<String> grid = new Grid<>();
        grid.addColumn(string -> String.valueOf(Math.random()))
                .setHeader("column 1");
        grid.addColumn(string -> String.valueOf(Math.random()))
                .setHeader("column 2");
        grid.addColumn(string -> String.valueOf(Math.random()))
                .setHeader("column 3");
        grid.addColumn(string -> String.valueOf(Math.random()))
                .setHeader("column 4");
        grid.addColumn(string -> String.valueOf(Math.random()))
                .setHeader("column 5");
        grid.addColumn(string -> String.valueOf(Math.random()))
                .setHeader("column 6");
        grid.addColumn(value -> LocalDate.now()).setHeader("date");

        grid.setWidth("100%");
        grid.setHeight("300px");

        grid.setItems(
                IntStream.rangeClosed(1, 100500).mapToObj(String::valueOf));
        add(grid);
    }

}
