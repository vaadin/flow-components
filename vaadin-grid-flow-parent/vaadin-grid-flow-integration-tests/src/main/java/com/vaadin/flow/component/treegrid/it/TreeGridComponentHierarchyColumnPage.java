package com.vaadin.flow.component.treegrid.it;

import static com.vaadin.flow.component.treegrid.it.TreeGridHugeTreePage.addItems;
import static com.vaadin.flow.component.treegrid.it.TreeGridHugeTreePage.addRootItems;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.Route;

@Route("treegrid-component-hierarchy-column")
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

        TreeData<String> data = new TreeData<>();
        final Map<String, String> parentPathMap = new HashMap<>();

        addRootItems("Granddad", 3, data, parentPathMap).forEach(
                granddad -> addItems("Dad", 3, granddad, data, parentPathMap)
                        .forEach(dad -> addItems("Son", 100, dad, data,
                                parentPathMap)));

        grid.setDataProvider(new TreeDataProvider<String>(data));

        add(grid);
    }

    private TextField createTextField(String value) {
        TextField textField = new TextField();
        textField.setReadOnly(true);
        textField.setValue(value);
        return textField;
    }

}
