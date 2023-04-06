/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.Arrays;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/grid-on-flex-layout")
public class GridOnFlexLayoutPage extends FlexLayout {

    public GridOnFlexLayoutPage() {
        setSizeFull();

        Grid<String> grid = new Grid<>();
        grid.setId("full-size-grid");
        grid.setItems(Arrays.asList("Item 1", "Item 2", "Item 3"));
        grid.addColumn(ValueProvider.identity());
        grid.setSizeFull();

        add(grid);
    }

}
