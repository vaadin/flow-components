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

import java.util.List;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/treegrid-expanded-auto-width-preserve-on-refresh")
@PreserveOnRefresh
public class TreeGridExpandedAutoWidthPreserveOnRefreshPage
        extends VerticalLayout {

    public TreeGridExpandedAutoWidthPreserveOnRefreshPage() {
        var grid = new TreeGrid<String>();
        grid.setAllRowsVisible(true);
        grid.addHierarchyColumn(String::toString).setHeader("Name")
                .setAutoWidth(true).setFlexGrow(0);

        var items = List.of("Item 1", "Parent 1");
        grid.setItems(items, item -> {
            if (item.startsWith("Parent")) {
                var index = Integer.parseInt(item.split(" ")[1]);
                if (index < 6) {
                    return List.of("Parent " + (index + 1));
                } else {
                    return List.of("Item");
                }
            }
            return List.of();
        });
        grid.expandRecursively(items, Integer.MAX_VALUE);

        add(grid);
    }
}
