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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

/**
 * PROTOTYPE: https://github.com/vaadin/flow-components/issues/10353
 */
@Route("vaadin-grid/optimistic-row-reorder")
@JavaScript("DragAndDropHelpers.js")
public class OptimisticRowReorderPage extends Div {

    private List<String> draggedItems = List.of();
    private boolean rejectDrops;
    private int serverDelay;

    public OptimisticRowReorderPage() {
        Grid<String> grid = new Grid<>();
        grid.addColumn(item -> item).setHeader("Item");
        GridListDataView<String> dataView = grid.setItems(new ArrayList<>(
                IntStream.range(0, 100).mapToObj(i -> "Item " + i).toList()));
        grid.setRowsDraggable(true);
        grid.setDropMode(GridDropMode.BETWEEN);
        grid.setOptimisticRowReorder(true);

        Div order = new Div();
        order.setId("server-order");

        grid.addDragStartListener(e -> draggedItems = e.getDraggedItems());
        grid.addDragEndListener(e -> draggedItems = List.of());
        grid.addDropListener(e -> {
            sleep(serverDelay);
            String target = e.getDropTargetItem().orElse(null);
            if (rejectDrops || target == null
                    || draggedItems.contains(target)) {
                return;
            }
            draggedItems.forEach(dataView::removeItem);
            if (e.getDropLocation() == GridDropLocation.ABOVE) {
                draggedItems
                        .forEach(item -> dataView.addItemBefore(item, target));
            } else {
                draggedItems.reversed()
                        .forEach(item -> dataView.addItemAfter(item, target));
            }
            order.setText(dataView.getItems().limit(5)
                    .collect(Collectors.joining(",")));
        });

        NativeButton reject = new NativeButton("Reject drops",
                e -> rejectDrops = true);
        reject.setId("reject-drops");
        NativeButton slow = new NativeButton("Slow server (1 s)",
                e -> serverDelay = 1000);
        slow.setId("slow-server");

        add(grid, reject, slow, order);
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
