/*
 * Copyright 2000-2024 Vaadin Ltd.
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
