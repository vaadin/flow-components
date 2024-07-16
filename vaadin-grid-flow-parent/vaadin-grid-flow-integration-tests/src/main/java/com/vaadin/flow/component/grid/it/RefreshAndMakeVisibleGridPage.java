/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.ArrayList;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/refresh-invisible-grid")
public class RefreshAndMakeVisibleGridPage extends Div {

    private Grid<String> grid;
    private ListDataProvider<String> dataProvider;

    public RefreshAndMakeVisibleGridPage() {
        grid = new Grid<>();
        dataProvider = new ListDataProvider<>(new ArrayList<>());
        grid.setDataProvider(dataProvider);
        grid.setVisible(false);
        grid.addColumn(ValueProvider.identity()).setHeader("Name");

        NativeButton button = new NativeButton("Make grid visible", event -> {
            dataProvider.getItems().clear();
            dataProvider.getItems().add("foo");
            dataProvider.refreshAll();
            grid.setVisible(true);
        });

        button.setId("refresh");

        add(grid, button);
    }

}
