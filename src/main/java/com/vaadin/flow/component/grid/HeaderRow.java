/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.AbstractRow.AbstractCell;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;

/**
 * One row of {@link HeaderCell}s in a Grid.
 * 
 * @author Vaadin Ltd.
 */
public class HeaderRow extends AbstractRow<HeaderCell> {

    /**
     * A header cell in a Grid.
     * 
     * @author Vaadin Ltd.
     */
    public static class HeaderCell extends AbstractCell {

        /**
         * Creates a new HeaderCell which wraps the given column element.
         * 
         * @param column
         */
        HeaderCell(AbstractColumn<?> column) {
            super(column);
        }

        @Override
        public void setText(String text) {
            getColumn().setHeaderText(text);
        }

        @Override
        public void setComponent(Component component) {
            getColumn().setHeaderComponent(component);
        }

    }

    /**
     * Creates a new header row from the layer of column elements.
     * 
     * @param layer
     */
    HeaderRow(ColumnLayer layer) {
        super(layer, HeaderCell::new);
    }

    /**
     * Joins the cells corresponding the given columns in the row.
     * 
     * @param columnsToMerge
     *            the columns of the cells that should be merged
     * @return the merged cell
     * @see #join(Collection)
     */
    public HeaderCell join(Column<?>... columnsToMerge) {
        return join(Arrays.stream(columnsToMerge).map(this::getCell)
                .collect(Collectors.toList()));
    }

    /**
     * Replaces the given cells with a new cell that takes the full space of the
     * joined cells.
     * <p>
     * The cells to join must be adjacent cells in this row, and this row must
     * be the out-most row.
     * 
     * @param cells
     *            the cells to join
     * @return the merged cell
     */
    public HeaderCell join(HeaderCell... cells) {
        return join(Arrays.asList(cells));
    }

    /**
     * Replaces the given cells with a new cell that takes the full space of the
     * joined cells.
     * <p>
     * The cells to join must be adjacent cells in this row, and this row must
     * be the out-most row.
     * 
     * @param cells
     *            the cells to join
     * @return the merged cell
     */
    public HeaderCell join(Collection<HeaderCell> cells) {
        Grid<?> grid = layer.getGrid();
        if (equals(grid.getDefaultHeaderRow())) {
            throw new UnsupportedOperationException(
                    "Cells cannot be joined on the first created header row. "
                            + "This row is used as the default row for setting column "
                            + "headers and for displaying sorting indicators, so the cells "
                            + "need to have 1-1 relationship with the columns.");
        }
        if (!isOutmostRow()) {
            throw new IllegalArgumentException(
                    "Cells can be joined only on the out-most row");
        }
        if (grid.getColumnLayers().indexOf(layer) == 0) {
            throw new IllegalArgumentException(
                    "Cells can not be joined on the bottom header row");
        }
        if (cells.size() < 2) {
            throw new IllegalArgumentException("Cannot join less than 2 cells");
        }
        if (!this.cells.containsAll(cells)) {
            throw new IllegalArgumentException(
                    "Cannot join cells that don't belong to this row");
        }

        List<HeaderCell> sortedCells = cells.stream().sorted((c1, c2) -> Integer
                .compare(this.cells.indexOf(c1), this.cells.indexOf(c2)))
                .collect(Collectors.toList());

        int cellInsertIndex = this.cells.indexOf(sortedCells.get(0));
        IntStream.range(0, sortedCells.size()).forEach(i -> {
            if (this.cells.indexOf(sortedCells.get(i)) != cellInsertIndex + i) {
                throw new IllegalArgumentException(
                        "Cannot join cells that are not adjacent");
            }
        });

        List<AbstractColumn<?>> columnsToJoin = cells.stream()
                .map(HeaderCell::getColumn).collect(Collectors.toList());

        int elementInsertIndex = columnsToJoin.stream()
                .mapToInt(
                        col -> grid.getElement().indexOfChild(col.getElement()))
                .min().getAsInt();
        columnsToJoin.forEach(col -> col.getElement().removeFromParent());

        List<AbstractColumn<?>> childColumns = new ArrayList<>();
        columnsToJoin.forEach(col -> childColumns
                .addAll(((ColumnGroup) col).getChildColumns()));

        ColumnGroup group = new ColumnGroup(grid, childColumns);

        layer.getGrid().getElement().insertChild(elementInsertIndex,
                group.getElement());
        layer.addColumn(cellInsertIndex, group);

        layer.getColumns().removeAll(columnsToJoin);

        this.cells.removeAll(cells);

        return this.cells.get(cellInsertIndex);
    }

    @Override
    protected boolean isOutmostRow() {
        List<ColumnLayer> layers = layer.getGrid().getColumnLayers();

        for (int i = layers.size() - 1; i >= 0; i--) {
            ColumnLayer layer = layers.get(i);
            if (layer.isHeaderRow()) {
                return equals(layer.asHeaderRow());
            }
        }
        return false;
    }

}
