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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.dom.ElementUtil;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/inert-tree-grid")
public class InertTreeGridPage extends Div {

    public InertTreeGridPage() {
        var grid = new TreeGrid<String>();
        ElementUtil.setInert(grid.getElement(), true);

        grid.addHierarchyColumn(String::toString).setHeader("Item");
        add(grid);

        var data = new TreeGridStringDataBuilder().addLevel("Parent", 100)
                .addLevel("Child", 100).build();

        grid.setDataProvider(new TreeDataProvider<>(data));

        var expandFirst = new NativeButton("Expand first item",
                e -> grid.expand(data.getRootItems().get(0)));
        expandFirst.setId("expand-first");

        var setAllRowsVisible = new NativeButton("Set all rows visible",
                e -> grid.setAllRowsVisible(true));
        setAllRowsVisible.setId("set-all-rows-visible");

        add(expandFirst, setAllRowsVisible, grid);
    }
}
