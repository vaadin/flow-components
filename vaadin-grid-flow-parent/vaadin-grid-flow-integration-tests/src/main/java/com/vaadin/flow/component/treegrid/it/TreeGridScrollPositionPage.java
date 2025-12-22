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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/treegrid-scroll-position")
public class TreeGridScrollPositionPage extends Div {
    private TestTreeData treeData = new TestTreeData(50, 50, 50);

    public TreeGridScrollPositionPage() {
        TreeGrid<TestTreeData.Item> treeGrid = new TreeGrid<>();
        treeGrid.setTreeData(treeData);
        treeGrid.addHierarchyColumn(TestTreeData.Item::getName);
        treeGrid.expandRecursively(treeData.getRootItems(), 2);
        treeGrid.addAttachListener(event -> {
            var item_25 = treeData.getRootItems().get(25);
            var item_25_25 = treeData.getChildren(item_25).get(25);
            var item_25_25_25 = treeData.getChildren(item_25_25).get(25);
            treeGrid.scrollToItem(item_25_25_25);
        });
        add(treeGrid);

        addButton("set-rows-draggable",
                event -> treeGrid.setRowsDraggable(true));

        addButton("set-drag-filter",
                event -> treeGrid.setDragFilter(item -> true));

        addButton("set-drop-filter",
                event -> treeGrid.setDropFilter((item) -> true));

        addButton("set-drop-mode",
                event -> treeGrid.setDropMode(GridDropMode.ON_TOP));

        addButton("set-drag-data-generator", event -> treeGrid
                .setDragDataGenerator("customProp", (item) -> "customValue"));

        addButton("set-part-name-generator", event -> treeGrid
                .setPartNameGenerator((item) -> "custom-part"));

        addButton("set-tooltip-generator", event -> treeGrid
                .setTooltipGenerator(item -> "Tooltip for " + item.getName()));

        addButton("add-column",
                event -> treeGrid.addColumn(TestTreeData.Item::getName));

        addButton("set-column-custom-renderer",
                event -> treeGrid.getColumns().get(0)
                        .setRenderer(new TextRenderer<>(
                                item -> "Rendered " + item.getName())));

        addButton("set-column-tooltip-generator",
                event -> treeGrid.getColumns().get(0)
                        .setTooltipGenerator(
                                item -> "Column tooltip for " + item.getName()));

        addButton("set-column-part-name-generator",
                event -> treeGrid.getColumns().get(0)
                        .setPartNameGenerator(item -> "column-custom-part"));
    }

    private void addButton(String id,
            ComponentEventListener<ClickEvent<NativeButton>> listener) {
        NativeButton button = new NativeButton(id.replaceAll("-", " "), listener);
        button.setId(id);
        add(button);
    }
}
