package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSelectionModel;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Route("vaadin-grid/grid-single-selection")
public class GridSingleSelectionPage extends Div {

    public GridSingleSelectionPage() {
        List<Integer> items = IntStream.range(0, 500).boxed()
                .collect(Collectors.toList());
        Grid<Integer> grid = new Grid<>();
        grid.setItems(items);
        grid.addColumn(ValueProvider.identity()).setHeader("Item");
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        GridSelectionModel<Integer> selectionModel = grid.getSelectionModel();

        NativeButton toggleFirstItem = new NativeButton("Toggle first item",
                e -> {
                    Integer firstItem = items.get(0);
                    if (selectionModel.isSelected(firstItem)) {
                        selectionModel.deselect(firstItem);
                    } else {
                        selectionModel.select(firstItem);
                    }
                });
        toggleFirstItem.setId("toggle-first-item");

        NativeButton toggleLastItem = new NativeButton("Toggle last item",
                e -> {
                    Integer lastItem = items.get(items.size() - 1);
                    if (selectionModel.isSelected(lastItem)) {
                        selectionModel.deselect(lastItem);
                    } else {
                        selectionModel.select(lastItem);
                    }
                });
        toggleLastItem.setId("toggle-last-item");

        NativeButton deselectAll = new NativeButton("Deselect all", e -> {
            grid.deselectAll();
        });
        deselectAll.setId("deselect-all");

        Div selectionLog = new Div();
        selectionLog.setId("selection-log");
        grid.asSingleSelect()
                .addValueChangeListener(event -> selectionLog.setText(
                        String.format("oldValue=%s; newValue=%s; fromClient=%s",
                                event.getOldValue(), event.getValue(),
                                event.isFromClient())));

        add(grid);
        add(new Div(toggleFirstItem, toggleLastItem, deselectAll));
        add(selectionLog);
    }
}
