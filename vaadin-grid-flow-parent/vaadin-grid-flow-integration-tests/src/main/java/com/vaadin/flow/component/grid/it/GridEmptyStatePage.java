package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/empty-state")
public class GridEmptyStatePage extends Div {
    public GridEmptyStatePage() {
        var grid = new Grid<>(Person.class);
        add(grid);

        // Button to set the empty state content
        var setEmptyStateContentButton = new Button("Set empty state content",
                event -> grid.setEmptyStateText("Custom empty state content"));
        setEmptyStateContentButton.setId("set-empty-state-content");

        // Button to set the grid items
        var setItemsButton = new Button("Set items", event -> grid
                .setItems(new Person("John", 20), new Person("Jane", 30)));
        setItemsButton.setId("set-items");

        // Button to clear the grid items
        var clearItemsButton = new Button("Clear items",
                event -> grid.setItems());
        clearItemsButton.setId("clear-items");

        add(setEmptyStateContentButton, setItemsButton, clearItemsButton);
    }
}
