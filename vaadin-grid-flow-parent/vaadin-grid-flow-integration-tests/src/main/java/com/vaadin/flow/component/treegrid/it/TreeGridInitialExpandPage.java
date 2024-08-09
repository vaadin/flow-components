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
        treeGrid.setAllRowsVisible(true);
        treeGrid.expand("parent1");
        treeGrid.expand("parent2");
        add(treeGrid);
    }
}
