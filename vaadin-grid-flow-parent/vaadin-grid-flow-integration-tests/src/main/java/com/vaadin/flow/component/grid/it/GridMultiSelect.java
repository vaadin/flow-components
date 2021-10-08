package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.stream.IntStream;

@Route("vaadin-grid/grid-multi-select")
public class GridMultiSelect extends VerticalLayout {
    public GridMultiSelect() {
        Grid<String> grid = new Grid<>();
        grid.setItems(IntStream.range(0,2).mapToObj(Integer::toString));

        grid.addColumn(i -> i).setHeader("text");
        grid.addColumn(i -> String.valueOf(i.length())).setHeader("length");

        grid.setSelectionMode(Grid.SelectionMode.MULTI);

        grid.getSelectionModel().select("0");
        grid.getSelectionModel().select("1");

        add(grid);
    }
}
