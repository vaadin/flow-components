package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/treegrid-component-hierarchy-column")
public class TreeGridComponentHierarchyColumnPage extends Div {

    public TreeGridComponentHierarchyColumnPage() {
        setSizeFull();

        getStyle().set("display", "flex");
        getStyle().set("flex-direction", "column");

        TreeGrid<String> grid = new TreeGrid<>();
        grid.setWidth("100%");
        grid.getStyle().set("flex", "1");

        grid.addComponentHierarchyColumn(this::createTextField)
                .setHeader("Header");

        TreeData<String> data = new TreeGridStringDataBuilder()
                .addLevel("Granddad", 3).addLevel("Dad", 3).addLevel("Son", 100)
                .build();

        grid.setDataProvider(new TreeDataProvider<>(data));

        add(grid);
    }

    private TextField createTextField(String value) {
        TextField textField = new TextField();
        textField.setReadOnly(true);
        textField.setValue(value);
        return textField;
    }

}
