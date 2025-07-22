/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
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
        TreeGrid<ItemTreeData.Item> grid = new TreeGrid<>();
        if (addGridBefore) {
            grid.setId("grid-then-comp");
            add(grid);
        }

        ComponentRenderer<TextField, ItemTreeData.Item> componentRenderer = new ComponentRenderer<>(
                TextField::new, (component, item) -> {
                    component.setReadOnly(true);
                    component.setValue(item.getName());
                });

        grid.addComponentHierarchyColumn(this::createTextField)
                .setHeader("Header A").setId("textfield");
        grid.addColumn(componentRenderer).setHeader("Header B");

        ComponentRenderer<Button, ItemTreeData.Item> componentRendererBtn = new ComponentRenderer<>(
                () -> new Button("btn"), ((button, s) -> {
                    button.setText(s.getName());
                    button.setThemeName(
                            ButtonVariant.LUMO_ERROR.getVariantName());
                }));
        grid.addColumn(componentRendererBtn).setHeader("Header C");

        grid.setTreeData(new ItemTreeData(3, 3, 100));
        if (!addGridBefore) {
            grid.setId("comp-then-grid");
            add(grid);
        }
    }

    private TextField createTextField(ItemTreeData.Item item) {
        TextField textField = new TextField();
        textField.setReadOnly(true);
        textField.setValue(item.getName());
        return textField;
    }
}
