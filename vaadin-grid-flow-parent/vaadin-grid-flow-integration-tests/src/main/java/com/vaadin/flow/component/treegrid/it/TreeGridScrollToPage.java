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

import java.util.Arrays;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/treegrid-scroll-to")
public class TreeGridScrollToPage extends Div {

    public TreeGridScrollToPage() {
        TreeGrid<String> grid = new TreeGrid<>();
        grid.setPageSize(10);
        grid.addHierarchyColumn(String::toString).setHeader("Item");

        add(grid);

        TreeData<String> data = new TreeGridStringDataBuilder()
                .addLevel("Granddad", 50).addLevel("Dad", 20)
                .addLevel("Son", 20).build();

        grid.setDataProvider(new TreeDataProvider<>(data));

        NativeButton expandAll = new NativeButton("Expand all",
                e -> grid.expandRecursively(data.getRootItems(), 3));
        expandAll.setId("expand-all");

        NativeButton scrollToStart = new NativeButton("Scroll to start",
                e -> grid.scrollToStart());
        scrollToStart.setId("scroll-to-start");

        NativeButton scrollToEnd = new NativeButton("Scroll to end",
                e -> grid.scrollToEnd());
        scrollToEnd.setId("scroll-to-end");

        Input scrollToIndex = new Input(ValueChangeMode.ON_BLUR);
        scrollToIndex.setPlaceholder("Scroll to index (format: 30-1-1)");
        scrollToIndex.setWidth("200px");
        scrollToIndex.addValueChangeListener(event -> {
            int[] path = Arrays.stream(event.getValue().split("-"))
                    .mapToInt(Integer::parseInt).toArray();
            grid.scrollToIndex(path);
        });
        scrollToIndex.setId("scroll-to-index");

        NativeButton changePageSize = new NativeButton("Change page size (2)",
                e -> grid.setPageSize(2));
        changePageSize.setId("change-page-size");

        add(grid, expandAll, scrollToStart, scrollToEnd, scrollToIndex,
                changePageSize);
    }
}
