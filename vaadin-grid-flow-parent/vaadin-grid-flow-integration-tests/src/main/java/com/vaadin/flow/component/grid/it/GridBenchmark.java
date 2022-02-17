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
package com.vaadin.flow.component.grid.it;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;

import org.slf4j.LoggerFactory;

// Example: http://localhost:8080/vaadin-grid/benchmark?variant=simple&metric=rendertime
@Route("vaadin-grid/benchmark")
@JsModule("./benchmark.js")
public class GridBenchmark extends Div implements HasUrlParameter<String> {

    private Grid<String> grid;

    private static final List<String> items = Collections
            .unmodifiableList(IntStream.range(0, 1000).mapToObj(String::valueOf)
                    .collect(Collectors.toList()));
    private static final TreeData<String> treeData = new TreeData<>();

    static {
        addTreeItems(treeData, null, 1000, 2);
    }

    private static void addTreeItems(TreeData<String> treeData, String parent,
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

    @Override
    public void setParameter(BeforeEvent event,
            @OptionalParameter String parameter) {
        Location location = event.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();

        Map<String, List<String>> parametersMap = queryParameters
                .getParameters();
        if (!parametersMap.containsKey("variant")
                || !parametersMap.containsKey("metric")) {
            add(new Text("Provide query parameters: variant and metric"));
            return;
        }
        String metric = parametersMap.get("metric").get(0);
        String variant = parametersMap.get("variant").get(0);

        LoggerFactory.getLogger(GridBenchmark.class)
                .info("Sample: " + variant + "-" + metric);

        switch (variant) {
        case "simple":
            grid = getGrid();
            addColumns(grid, 5, false);
            break;
        case "multicolumn":
            grid = getGrid();
            addColumns(grid, 50, false);
            break;
        case "componentrenderers":
            grid = getGrid();
            addColumns(grid, 5, true);
            break;
        case "detailsopened":
            grid = getGrid();
            addColumns(grid, 5, false);
            grid.setItemDetailsRenderer(
                    new ComponentRenderer<>(item -> new Text(item.toString())));
            items.forEach(item -> grid.setDetailsVisible(item, true));
            break;
        case "tree":
            grid = getTreeGrid();
            ((TreeGrid<String>) grid).addHierarchyColumn(i -> i);
            addColumns(grid, 5, false);
            break;
        case "mixed":
            grid = getTreeGrid();
            ((TreeGrid<String>) grid).addHierarchyColumn(i -> i);
            addColumns(grid, 50, true);
            grid.setItemDetailsRenderer(
                    new ComponentRenderer<>(item -> new Text(item.toString())));
            treeData.getRootItems()
                    .forEach(item -> grid.setDetailsVisible(item, true));
            break;
        default:
            break;
        }

        switch (metric) {
        case "verticalscrollframetime":
            add(grid);
            whenRendered(grid).then(v -> grid.getElement()
                    .executeJs("window.measureScrollFrameTime(this, false)"));
            break;
        case "horizontalscrollframetime":
            add(grid);
            whenRendered(grid).then(v -> grid.getElement()
                    .executeJs("window.measureScrollFrameTime(this, true)"));
            break;
        case "rendertime":
            measureRendered(grid);
            UI.getCurrent().getElement()
                    .executeJs("return window.startWhenReady()")
                    .then(v -> add(grid));
            break;
        case "expandtime":
            add(grid);
            startWhenRendered(grid).then(v -> {
                measureRendered(grid);
                TreeGrid<String> treeGrid = (TreeGrid<String>) grid;
                TreeData<String> data = ((TreeDataProvider<String>) treeGrid
                        .getDataProvider()).getTreeData();
                treeGrid.expandRecursively(data.getRootItems(), 5);
            });
            break;
        default:
            break;
        }
    }

    private PendingJavaScriptResult whenRendered(Grid<String> grid) {
        return grid.getElement().executeJs("return window.whenRendered(this)");
    }

    private PendingJavaScriptResult startWhenRendered(Grid<String> grid) {
        return grid.getElement()
                .executeJs("return window.startWhenRendered(this)");
    }

    private void measureRendered(Grid<String> grid) {
        grid.getElement().executeJs("window.measureRender(this)");
    }

    private Grid<String> getGrid() {
        Grid<String> result = new Grid<>();
        result.setItems(items);
        result.getElement().setAttribute("benchmark", true);
        return result;
    }

    private TreeGrid<String> getTreeGrid() {
        TreeGrid<String> result = new TreeGrid<>();
        result.setTreeData(treeData);
        result.getElement().setAttribute("benchmark", true);
        return result;
    }

    private void addColumns(Grid<String> grid, int count,
            boolean componentrenderers) {
        IntStream.range(0, count).forEach(index -> {
            if (componentrenderers) {
                grid.addColumn(
                        new ComponentRenderer<>(item -> new NativeButton(item)))
                        .setHeader(String.valueOf(index));
            } else {
                grid.addColumn(item -> item).setHeader(String.valueOf(index));
            }
        });
    }

}
