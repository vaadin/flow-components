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
package com.vaadin.flow.component.grid.it;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.ColumnRendering;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

/**
 * Test page for Grid column scrolling functionality.
 */
@Route("vaadin-grid/grid-column-scroll")
public class GridColumnScrollPage extends Div {

    public GridColumnScrollPage() {
        Grid<TestItem> grid = new Grid<>();

        // Set width to ensure not all columns are visible at once
        grid.setWidth("800px");

        // Enable lazy column rendering
        grid.setColumnRendering(ColumnRendering.LAZY);

        // Add many columns to ensure horizontal scrolling is needed
        for (int i = 0; i < 20; i++) {
            final int columnIndex = i;
            Column<TestItem> column = grid
                    .addColumn(item -> item.getValue(columnIndex))
                    .setHeader("Column " + i).setWidth("150px");
            column.setKey("col" + i);
        }

        // Add test data
        List<TestItem> items = new ArrayList<>();
        for (int row = 0; row < 100; row++) {
            items.add(new TestItem(row));
        }
        grid.setItems(items);

        add(grid);
    }

    public static class TestItem {
        private final int rowIndex;

        public TestItem(int rowIndex) {
            this.rowIndex = rowIndex;
        }

        public String getValue(int columnIndex) {
            return "R" + rowIndex + "C" + columnIndex;
        }
    }
}
