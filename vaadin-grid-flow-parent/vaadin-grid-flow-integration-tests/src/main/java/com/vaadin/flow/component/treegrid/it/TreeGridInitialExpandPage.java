package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/treegrid-initial-expand")
public class TreeGridInitialExpandPage extends Div {

    public TreeGridInitialExpandPage() {
        TreeGrid<String> treeGrid = new TreeGrid<>();
        treeGrid.addHierarchyColumn(String::toString).setHeader("String");
        TreeData<String> data = new TreeData<>();
        data.addItem(null, "parent1");
        data.addItem("parent1", "parent1-child1");
        data.addItem("parent1", "parent1-child2");
        data.addItem(null, "parent2");
        data.addItem("parent2", "parent2-child2");
        treeGrid.setDataProvider(new TreeDataProvider<>(data));
        treeGrid.setHeightByRows(true);
        treeGrid.expand("parent1");
        treeGrid.expand("parent2");
        add(treeGrid);
    }
}
