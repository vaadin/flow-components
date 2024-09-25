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
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/treegrid-changing-hierarchy")
public class TreeGridChangingHierarchyPage extends Div {
    public TreeGridChangingHierarchyPage() {
        TreeData<String> data = new TreeData<>();
        data.addItems(null, "a", "b", "c").addItem("b", "b/a");

        TreeGrid<String> grid = new TreeGrid<>();
        grid.setDataProvider(new TreeDataProvider<>(data));
        grid.addHierarchyColumn(ValueProvider.identity());

        NativeButton btn = new NativeButton("add items to a and refresh");
        btn.addClickListener(event -> {
            data.addItems("a", "a/a", "a/b");
            grid.getDataProvider().refreshItem("a", true);
        });
        NativeButton btn2 = new NativeButton("add items to a/a and refresh");
        btn2.addClickListener(event -> {
            data.addItems("a/a", "a/a/a", "a/a/c").addItem("a/a/a", "a/a/a/a");
            grid.getDataProvider().refreshItem("a/a", true);
        });
        NativeButton btn3 = new NativeButton("remove a/a");
        btn3.addClickListener(event -> {
            data.removeItem("a/a");
            // Inform item removal to DataProvider
            grid.getDataProvider().refreshAll();
        });
        NativeButton btn4 = new NativeButton("remove children of a/a");
        btn4.addClickListener(event -> {
            data.removeItem("a/a/a");
            data.removeItem("a/a/c");
            // Inform item removal to DataProvider
            grid.getDataProvider().refreshAll();
        });
        NativeButton btn5 = new NativeButton("remove a");
        btn5.addClickListener(event -> {
            data.removeItem("a");
            // Inform item removal to DataProvider
            grid.getDataProvider().refreshAll();
        });
        NativeButton btn6 = new NativeButton("remove children of a");
        btn6.addClickListener(event -> {
            data.removeItem("a/a");
            data.removeItem("a/b");
            // Inform item removal to DataProvider
            grid.getDataProvider().refreshAll();
        });
        NativeButton btn7 = new NativeButton("remove children of a/a/a");
        btn7.addClickListener(event -> {
            data.removeItem("a/a/a/a");
            // All the children of an item are removed so it should be collapsed
            // (otherwise TreeGrid will get stuck in loading state)
            grid.collapse("a/a/a");
            // Inform item removal to DataProvider
            grid.getDataProvider().refreshAll();
        });

        NativeButton btn8 = new NativeButton("move c under a");
        btn8.addClickListener(event -> {
            data.setParent("c", "a");
            grid.getDataProvider().refreshAll();
            grid.expand("a");
        });

        NativeButton btn9 = new NativeButton("check key of c");
        btn9.addClickListener(event -> {
            btn8.setText(String.valueOf(
                    grid.getDataCommunicator().getKeyMapper().has("c")));
        });

        add(grid, btn, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9);
    }
}
