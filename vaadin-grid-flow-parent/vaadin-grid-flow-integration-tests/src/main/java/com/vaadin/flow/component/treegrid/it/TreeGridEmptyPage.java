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
