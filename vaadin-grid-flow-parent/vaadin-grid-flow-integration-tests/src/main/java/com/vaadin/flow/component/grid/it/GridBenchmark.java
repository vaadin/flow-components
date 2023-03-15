/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.grid.it;

import java.util.List;
import java.util.stream.IntStream;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.router.Route;

@Route("grid")
@JsModule("./grid-benchmark.js")
public class GridBenchmark extends AbstractBenchmark {

    private TreeGrid<String> grid;

    @Override
    protected void runMetric(String metric) {
        switch (metric) {
        case "verticalscrollframetime":
            add(grid);
            whenRendered(grid).then(v -> grid.getElement()
                    .executeJs("window.measureScrollFrameTime(this)"));
            break;
        case "rendertime":
            measureRendered(grid);
            UI.getCurrent().getElement()
                    .executeJs("return window.startWhenReady()")
                    .then(v -> add(grid));
            break;
        case "expandedrendertime":
            // Expand the TreeGrid nodes
            grid.expandRecursively(grid.getTreeData().getRootItems(), 5);
            // The test is considered complete when the grid is fully rendered
            measureRendered(grid);
            UI.getCurrent().getElement()
                    // Mark the test start timestamp then the UI is ready and
                    // idle
                    .executeJs("return window.startWhenReady()")
                    // Then add the grid to the UI
                    .then(v -> add(grid));
            break;
        case "expandtime":
            add(grid);
            startWhenRendered(grid).then(v -> {
                measureRendered(grid);
                grid.expandRecursively(grid.getTreeData().getRootItems(), 5);
            });
            break;
        case "selectalltime":
            add(grid);
            startWhenRendered(grid).then(v -> {
                measureRendered(grid);
                var rootItems = grid.getTreeData().getRootItems();
                var selectionModel = (GridMultiSelectionModel<String>) grid
                        .getSelectionModel();
                selectionModel.selectItems(rootItems.toArray(new String[0]));
            });
            break;
        case "sorttime":
            add(grid);
            startWhenRendered(grid).then(v -> {
                measureRendered(grid);
                grid.sort(List.of(new GridSortOrder<>(grid.getColumns().get(0),
                        SortDirection.DESCENDING)));
            });
            break;
        case "expandedsorttime":
            // Expand the TreeGrid nodes
            grid.expandRecursively(grid.getTreeData().getRootItems(), 5);
            add(grid);
            // Start the test when the grid with pre-expanded rows is fully
            // rendered
            startWhenRendered(grid).then(v -> {
                // The test is considered complete when the grid is fully
                // rerendered
                measureRendered(grid);
                // Apply the sort
                grid.sort(List.of(new GridSortOrder<>(grid.getColumns().get(0),
                        SortDirection.DESCENDING)));
            });
            break;
        case "scrolltoindextime":
            add(grid);
            startWhenRendered(grid).then(v -> {
                measureRendered(grid);
                grid.scrollToIndex(500);
            });
            break;
        default:
            add(grid);
            break;
        }
    }

    @Override
    protected void initBenchmark(Features features) {
        // Create the grid
        grid = new TreeGrid<>();

        // Set a non-default height
        grid.setHeight("800px");

        // Add items
        var treeData = new TreeData<String>();
        addTreeItems(treeData, null, 1000, 2);
        grid.setTreeData(treeData);

        // Multi-selection column
        if (features.hasFeature("multiselection")) {
            var selectionModel = (GridMultiSelectionModel<String>) grid
                    .setSelectionMode(Grid.SelectionMode.MULTI);
            selectionModel.setSelectionColumnFrozen(true);
        }

        // Hierarchy column
        if (features.hasFeature("hierarchycolumn")) {
            ((TreeGrid<String>) grid).addHierarchyColumn(i -> i);
        }

        // Component columns
        if (features.hasFeature("componentcolumns")) {
            IntStream.range(0, 30).forEach(index -> {
                grid.addComponentColumn(item -> new NativeButton(item));
            });
        }

        // Text columns
        if (features.hasFeature("textcolumns")) {
            IntStream.range(0, 30).forEach(index -> {
                grid.addColumn(item -> item);
            });
        }

        // Theme variant
        if (features.hasFeature("themevariant")) {
            grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        }

        // Sortable columns
        if (features.hasFeature("sortablecolumns")) {
            grid.getColumns().forEach(column -> column.setSortable(true));
        }

        // Resizable columns
        if (features.hasFeature("resizablecolumns")) {
            grid.getColumns().forEach(column -> column.setResizable(true));
        }

        // Auto-width columns
        if (features.hasFeature("autowidthcolumns")) {
            grid.getColumns().forEach(column -> column.setAutoWidth(true));
        }

        // Add headers and footers
        for (int index = 0; index < grid.getColumns().size(); index++) {
            var column = grid.getColumns().get(index);

            if (features.hasFeature("componentheaders")) {
                var headerInput = new Input();
                headerInput.setPlaceholder("Header " + index);
                column.setHeader(headerInput);
            } else {
                column.setHeader("Header " + index);
            }

            if (features.hasFeature("columnfooters")) {
                column.setFooter("Footer " + index);
            }
        }

        // Group header cells
        if (features.hasFeature("groupheadercells")) {
            var columnsArray = grid.getColumns()
                    .subList(1, grid.getColumns().size() - 2)
                    .toArray(new Grid.Column[0]);
            grid.prependHeaderRow().join(columnsArray).setText("Group header");
        }

        // Frozen columns
        if (features.hasFeature("frozencolumns")) {
            grid.getColumns().get(0).setFrozen(true);
            grid.getColumns().get(grid.getColumns().size() - 1)
                    .setFrozenToEnd(true);
        }

        // Class name generator
        if (features.hasFeature("classnamegenerator")) {
            grid.setClassNameGenerator(item -> "test-classname");
        }
    }

    /**
     * Adds items to the given tree data.
     */
    private void addTreeItems(TreeData<String> treeData, String parent,
            int count, int level) {
        IntStream.range(0, count).forEach(index -> {
            String child = parent != null ? parent + "-" + index
                    : String.valueOf(index);
            treeData.addItem(parent, child);
            if (level > 0) {
                addTreeItems(treeData, child, 3, level - 1);
            }
        });
    }

    /**
     * Returns a promise that resolves when the grid has been fully rendered.
     */
    private PendingJavaScriptResult whenRendered(Grid<String> grid) {
        return grid.getElement().executeJs("return window.whenRendered(this)");
    }

    /**
     * Returns a promise that resolves when the grid has been fully rendered and
     * start timestamp has been set.
     */
    private PendingJavaScriptResult startWhenRendered(Grid<String> grid) {
        return grid.getElement()
                .executeJs("return window.startWhenRendered(this)");
    }

    /**
     * Completes and reports the measurement when the given grid has been fully
     * rendered. Start timestamp needs to be marked separately.
     */
    private void measureRendered(Grid<String> grid) {
        grid.getElement().executeJs("window.measureRender(this)");
    }

}
