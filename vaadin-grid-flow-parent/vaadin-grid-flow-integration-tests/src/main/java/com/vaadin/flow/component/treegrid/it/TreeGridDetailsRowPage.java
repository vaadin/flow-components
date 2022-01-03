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
