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
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/treegrid-details-row")
public class TreeGridDetailsRowPage extends Div {

    public static final String PARENT_1_CHILD_1 = "parent1-child1";
    public static final String PARENT_1_CHILD_2 = "parent1-child2";
    public static final String PARENT_1 = "parent1";

    public TreeGridDetailsRowPage() {

        TreeGrid<String> treeGrid = new TreeGrid<>();
        treeGrid.addHierarchyColumn(String::toString).setHeader("String");
        TreeData<String> data = new TreeData<>();
        data.addItem(null, PARENT_1);
        data.addItem(PARENT_1, PARENT_1_CHILD_1);
        data.addItem(PARENT_1_CHILD_1, "p1-c1-c1");
        data.addItem(PARENT_1_CHILD_1, "p1-c1-c2");
        data.addItem(PARENT_1, PARENT_1_CHILD_2);
        data.addItem(PARENT_1_CHILD_2, "p1-c2-c1");
        data.addItem(PARENT_1_CHILD_2, "p1-c2-c2");
        data.addItem(null, "parent2");
        data.addItem("parent2", "parent2-child2");
        treeGrid.setDataProvider(new TreeDataProvider<>(data));

        treeGrid.setItemDetailsRenderer(new ComponentRenderer<>(
                (SerializableFunction<String, Button>) Button::new));
        treeGrid.setDetailsVisible(PARENT_1, true);

        add(treeGrid);
    }
}
