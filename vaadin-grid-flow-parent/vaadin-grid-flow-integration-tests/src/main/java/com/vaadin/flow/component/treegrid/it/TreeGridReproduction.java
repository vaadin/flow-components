package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.Route;

@Route("tree-grid-reproduction")
public class TreeGridReproduction extends Div {
    public TreeGridReproduction() {
        TreeGrid<String> tree = new TreeGrid<>(String.class);

        tree.removeAllColumns();

        tree.addHierarchyColumn(item -> item).setHeader("Column");

        tree.addColumn(item -> item.length() + "");

        tree.addComponentColumn(item -> new Button("Some component"));

        TreeData<String> treeData = new TreeData<>();

        TreeDataProvider<String> provider = new TreeDataProvider<>(treeData);

        tree.setDataProvider(provider);

        treeData.addRootItems("A", "B", "C");

        treeData.addItems("A", "A1", "A2");

        treeData.addItems("A1", "C1", "C2");

        treeData.addItems("C1", "D1", "D2");

        tree.getDataProvider().refreshAll();

        tree.getDataCommunicator().reset();



        add(tree);
    }
}
