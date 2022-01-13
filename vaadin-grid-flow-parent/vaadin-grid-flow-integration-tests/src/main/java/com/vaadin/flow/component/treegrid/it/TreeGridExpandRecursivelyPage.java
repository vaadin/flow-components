/*
 * Copyright 2000-2019 Vaadin Ltd.
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

import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

@Route("tree-grid-expand-recursively")
public class TreeGridExpandRecursivelyPage extends VerticalLayout {

    TreeGrid<String> grid;

    public TreeGridExpandRecursivelyPage() {
        grid = new TreeGrid<>();
        grid.setWidth("350px");
        grid.setMaxHeight("300px");

        grid.addComponentHierarchyColumn(value -> new Span(value))
                .setHeader("Name").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(value -> new Span(new Date().toString()))
                .setHeader("Datum");
        grid.addComponentColumn(value -> new Span(new Date().toString()))
                .setHeader("Datum 2");

        grid.addExpandListener(event -> {
            Span label = new Span("Expanded");
            label.setId("expanded");
            add(label);
        });

        grid.addCollapseListener(event -> {
            Span label = new Span("Collapsed");
            label.setId("collapsed");
            add(label);
        });

        TreeData<String> treeData = new TreeData<>();
        treeData.addRootItems("A");
        treeData.addItems("A", Arrays.asList("A 1", "B 1"));
        treeData.addItems("A 1", Arrays.asList(
                "Child A which may has a long title", "Child B", "Child C"));
        treeData.addItems("B 1",
                Arrays.asList("Child D", "Child E which may has a long title",
                        "Child C which may has a long title"));
        grid.setTreeData(treeData);

        NativeButton expand = new NativeButton("Expand", e -> {
            grid.expandRecursively(Collections.singleton("A"), 2);
        });
        expand.setId("expand");

        NativeButton collapse = new NativeButton("Collapse", e -> {
            grid.collapseRecursively(Collections.singleton("A"), 2);
        });
        collapse.setId("collapse");

        add(grid, expand, collapse);
    }
}
