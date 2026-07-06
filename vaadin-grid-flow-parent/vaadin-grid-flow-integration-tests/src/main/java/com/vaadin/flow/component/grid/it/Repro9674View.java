/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.Route;

/**
 * Repro for https://github.com/vaadin/flow-components/issues/9674 — a TreeGrid
 * placed inside a parent Grid's component column does not render its row
 * content after attach; the content only appears after interacting with the
 * parent Grid (e.g. sorting a column).
 */
@Route("repro-9674")
public class Repro9674View extends Div {

    public Repro9674View() {
        var grid = new Grid<String>();
        grid.setId("outer-grid");
        grid.addColumn(item -> "1").setHeader("Number").setSortable(true);
        grid.addComponentColumn(item -> createTreeGrid())
                .setHeader("Tree Grid Column");
        grid.setItems(List.of("row"));
        add(grid);
    }

    private TreeGrid<String> createTreeGrid() {
        var treeGrid = new TreeGrid<String>();
        treeGrid.addHierarchyColumn(s -> s).setHeader("Item");
        treeGrid.setItems(List.of("dummy text"), item -> List.of());
        return treeGrid;
    }
}
