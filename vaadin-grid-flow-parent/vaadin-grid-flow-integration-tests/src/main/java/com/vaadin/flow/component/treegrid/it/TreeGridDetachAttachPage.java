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
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/treegrid-detach-attach")
@PreserveOnRefresh
public class TreeGridDetachAttachPage extends Div {

    private TreeGrid<HierarchicalTestBean> grid;

    public TreeGridDetachAttachPage() {
        grid = new TreeGrid<>();
        grid.addHierarchyColumn(HierarchicalTestBean::toString);
        grid.setDataProvider(new LazyHierarchicalDataProvider(200, 1));
        add(grid);

        NativeButton toggleAttached = new NativeButton("toggle attached", e -> {
            if (grid.getParent().isPresent()) {
                remove(grid);
            } else {
                add(grid);
            }
        });
        toggleAttached.setId("toggle-attached");
        add(toggleAttached);

        NativeButton useAutoWidthColumn = new NativeButton(
                "use auto-width column", e -> {
                    grid.removeAllColumns();
                    grid.addHierarchyColumn(HierarchicalTestBean::toString)
                            .setAutoWidth(true).setFlexGrow(0);
                });
        useAutoWidthColumn.setId("use-auto-width-column");
        add(useAutoWidthColumn);
    }
}
