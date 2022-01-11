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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.bean.HierarchicalTestBean;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/treegrid-refresh-all")
public class TreeGridRefreshAllPage extends Div {

    public TreeGridRefreshAllPage() {
        TreeGrid<HierarchicalTestBean> grid = new TreeGrid<>();
        grid.addHierarchyColumn(HierarchicalTestBean::toString);
        LazyHierarchicalDataProvider dataProvider = new LazyHierarchicalDataProvider(
                50, 4);
        grid.setDataProvider(dataProvider);

        NativeButton refreshAll = new NativeButton("Refresh All",
                e -> grid.getDataProvider().refreshAll());
        refreshAll.setId("refresh-all");

        NativeButton clear = new NativeButton("clear", e -> {
            dataProvider.clear();
            dataProvider.refreshAll();
        });
        clear.setId("clear");

        add(grid, refreshAll, clear);
    }
}
