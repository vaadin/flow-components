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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.AbstractRow.AbstractCell;
import com.vaadin.flow.component.grid.Grid.Column;

/**
 * Base class for header and footer rows
 * 
 * @author Vaadin Ltd.
 */
abstract class AbstractRow<CELL extends AbstractCell> {

    /**
     * Base class for header and footer cells.
     * 
     * @author Vaadin Ltd.
     */
    public static abstract class AbstractCell {

        /*
         * This is the <vaadin-grid-column> or <vaadin-grid-column-group> that
         * contains the header or footer template.
         */
        private AbstractColumn<?> columnComponent;

        AbstractCell(AbstractColumn<?> column) {
            this.columnComponent = column;
        }

        protected void setColumn(AbstractColumn<?> column) {
            this.columnComponent = column;
        }

        protected AbstractColumn<?> getColumn() {
            return columnComponent;
        }

        /**
         * Sets the text content of this cell.
         * <p>
         * This will remove a component set with
         * {@link #setComponent(Component)}.
         * 
         * @param text
         *            the text to be shown in this cell
         */
        public abstract void setText(String text);

        /**
         * Sets the component as the content of this cell.
         * <p>
         * This will remove text set with {@link #setText(String)}.
         * 
         * @param component
         *            the component to set
         */
        public abstract void setComponent(Component component);

    }

    protected ColumnLayer layer;
    protected List<CELL> cells;

    private Function<AbstractColumn<?>, CELL> cellCtor;

    AbstractRow(ColumnLayer layer, Function<AbstractColumn<?>, CELL> cellCtor) {
        this.layer = layer;
        this.cellCtor = cellCtor;
        cells = layer.getColumns().stream().map(cellCtor)
                .collect(Collectors.toList());
    }

    /**
     * Change this row to wrap the given layer
     * 
     * @param layer
     *            the layer to wrap
     */
    protected void setLayer(ColumnLayer layer) {
        this.layer = layer;
        setColumns(layer.getColumns());
    }

    /**
     * Change the cells to wrap the given columns
     * 
     * @param columns
     *            new column components for the cells
     */
    protected void setColumns(List<AbstractColumn<?>> columns) {
        assert columns.size() == cells.size();

        IntStream.range(0, columns.size()).forEach(i -> {
            cells.get(i).setColumn(columns.get(i));
        });
    }

    protected void addCell(AbstractColumn<?> column) {
        cells.add(cellCtor.apply(column));
    }

    protected void addCell(int index, AbstractColumn<?> column) {
        cells.add(index, cellCtor.apply(column));
    }

    /**
     * Gets the cells that belong to this row as an unmodifiable list.
     * 
     * @return the cells on this row
     */
    public List<CELL> getCells() {
        return Collections.unmodifiableList(cells);
    }

    /**
     * Gets the cell on this row that is on the given column.
     * 
     * @param column
     *            the column to find cell for
     * @return the corresponding cell
     * @throws IllegalArgumentException
     *             if the column does not belong to the same grid as this row
     */
    public CELL getCell(Column<?> column) {
        return getCellFor(column);
    }

    private CELL getCellFor(AbstractColumn<?> column) {
        return getCells().stream().filter(cell -> cell.getColumn() == column)
                .findFirst().orElseGet(() -> {
                    Optional<Component> parent = column.getParent();
                    if (parent.isPresent()
                            && parent.get() instanceof AbstractColumn) {
                        return getCellFor((AbstractColumn<?>) parent.get());
                    } else {
                        throw new IllegalArgumentException(
                                "Cannot find a cell from this row that would "
                                        + "correspond to the given column");
                    }
                });
    }

    /**
     * Gets whether this is the top-most HeaderRow or the bottom-most FooterRow.
     * 
     * @return whether this is the outmost row
     */
    protected abstract boolean isOutmostRow();
}
