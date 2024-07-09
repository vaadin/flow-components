/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/treegrid-empty")
public class TreeGridEmptyPage extends Div {

    public TreeGridEmptyPage() {
        TreeGrid<String> grid = new TreeGrid<>();
        grid.setId("treegrid");
        grid.addHierarchyColumn(e -> e);

        add(grid);

        Button addExpandedButton = new Button("Add an empty expanded item",
                e -> {
                    TreeData<String> data = new TreeData<>();
                    data.addItems(null, "parent");
                    grid.setDataProvider(new TreeDataProvider<>(data));
                    grid.getDataProvider().refreshAll();
                    data.addItems("parent", "child");
                    grid.expand("parent");
                    data.removeItem("child");
                });
        addExpandedButton.setId("add-expanded-button");

        add(addExpandedButton);
    }

}
