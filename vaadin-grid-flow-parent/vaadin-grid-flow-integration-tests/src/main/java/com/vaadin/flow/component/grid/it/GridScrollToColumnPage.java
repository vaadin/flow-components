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

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/grid-scroll-to-column")
public class GridScrollToColumnPage extends Div {
    public GridScrollToColumnPage() {
        Grid<Integer> grid = new Grid<>();
        grid.setWidth("400px");

        var items = java.util.stream.IntStream.range(0, 10).boxed().toList();
        grid.setItems(items);

        final int columnCount = 20;
        for (int i = 0; i < columnCount; i++) {
            final int columnIndex = i;
            grid.addColumn(item -> "Cell " + columnIndex)
                    .setHeader("Column " + columnIndex)
                    .setKey("column-" + columnIndex);
        }

        NativeButton scrollByIndex = new NativeButton(
                "Scroll to column 10 by index", e -> grid.scrollToColumn(10));
        scrollByIndex.setId("scroll-by-index");
        add(scrollByIndex);

        NativeButton scrollByReference = new NativeButton(
                "Scroll to column 10 by reference", e -> {
                    var column = grid.getColumnByKey("column-10");
                    grid.scrollToColumn(column);
                });
        scrollByReference.setId("scroll-by-reference");
        add(scrollByReference);

        add(grid);
    }
}
