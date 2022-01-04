/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.treegrid.it;

import static com.vaadin.flow.component.treegrid.it.TreeGridHugeTreePage.addItems;
import static com.vaadin.flow.component.treegrid.it.TreeGridHugeTreePage.addRootItems;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;

/**
 * Test page for dynamically adding new columns with different renderers when
 * the TreeGrid is added.
 */
@Route("vaadin-grid/tree-component-columns")
public class TreeComponentColumnsPage extends Div {

    public TreeComponentColumnsPage() {
        addButton("btn-add-comp-then-grid", () -> addTreeGrid(false));
        addButton("btn-add-grid-then-comp", () -> addTreeGrid(true));
    }

    private void addButton(String id, Command action) {
        NativeButton button = new NativeButton(id, e -> action.execute());
        button.setId(id);
        add(button);
    }

    private void addTreeGrid(boolean addGridBefore) {
        TreeGrid<String> grid = new TreeGrid<>();
        if (addGridBefore) {
            grid.setId("grid-then-comp");
            add(grid);
        }

        ComponentRenderer<TextField, String> componentRenderer = new ComponentRenderer<>(
                TextField::new, (component, item) -> {
                    component.setReadOnly(true);
                    component.setValue(item);
                });

        grid.addComponentHierarchyColumn(this::createTextField)
                .setHeader("Header A").setId("textfield");
        grid.addColumn(componentRenderer).setHeader("Header B");

        ComponentRenderer<Button, String> componentRendererBtn = new ComponentRenderer<>(
                () -> new Button("btn"), ((button, s) -> {
                    button.setText(s);
                    button.setThemeName(
                            ButtonVariant.LUMO_ERROR.getVariantName());
                }));
        grid.addColumn(componentRendererBtn).setHeader("Header C");

        TreeData<String> data = new TreeData<>();
        final Map<String, String> parentPathMap = new HashMap<>();

        addRootItems("Granddad", 3, data, parentPathMap).forEach(
                granddad -> addItems("Dad", 3, granddad, data, parentPathMap)
                        .forEach(dad -> addItems("Son", 100, dad, data,
                                parentPathMap)));

        grid.setDataProvider(new TreeDataProvider<>(data));
        if (!addGridBefore) {
            grid.setId("comp-then-grid");
            add(grid);
        }
    }

    private TextField createTextField(String val) {
        TextField textField = new TextField();
        textField.setReadOnly(true);
        textField.setValue(val);
        return textField;
    }
}
