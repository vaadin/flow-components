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
package com.vaadin.flow.component.grid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.AbstractRow.AbstractCell;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableFunction;

/**
 * Base class for header and footer rows
 *
 * @author Vaadin Ltd.
 */
abstract class AbstractRow<CELL extends AbstractCell> implements Serializable {

    /**
     * Base class for header and footer cells.
     *
     * @author Vaadin Ltd.
     */
    public static abstract class AbstractCell implements Serializable {

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

    private SerializableFunction<AbstractColumn<?>, CELL> cellCtor;

    AbstractRow(ColumnLayer layer,
            SerializableFunction<AbstractColumn<?>, CELL> cellCtor) {
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

    protected void addCell(int index, AbstractColumn<?> column) {
        cells.add(index, cellCtor.apply(column));
    }

    protected void removeCell(AbstractColumn<?> columnComponent) {
        CELL cellToRemove = cells.stream()
                .filter(cell -> cell.getColumn().equals(columnComponent))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "removeCell() should never be called for a column component "
                                + "that doesn't have a corresponding cell in this row."));
        cells.remove(cellToRemove);
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
     * Joins the cells corresponding the given columns in the row.
     * <p>
     * The columns must be adjacent, and this row must be the out-most row.
     * <p>
     * The way that the client-side web component works also causes some
     * limitations to which cells can be joined. For example, if you join the
     * first and second cell in the header, you cannot join the second and third
     * cell in the footer. This is because the headers and footers use the same
     * elements in the client-side and it's not possible to create a
     * hierarchical DOM structure to support this case. You can, however, join
     * all three cells in the footer, because then it's again possible to
     * organize the client-side elements in a hierarchical structure.
     *
     * @param columns
     *            the columns of the cells that should be joined
     * @return the merged cell
     * @see #join(Collection)
     * @throws IllegalArgumentException
     *             if it's not possible to join the given cells
     */
    public CELL join(Column<?>... columns) {
        return join(Arrays.stream(columns).map(this::getCell)
                .collect(Collectors.toList()));
    }

    /**
     * Replaces the given cells with a new cell that takes the full space of the
     * joined cells.
     * <p>
     * The cells to join must be adjacent cells in this row, and this row must
     * be the out-most row.
     * <p>
     * The way that the client-side web component works also causes some
     * limitations to which cells can be joined. For example, if you join the
     * first and second cell in the header, you cannot join the second and third
     * cell in the footer. This is because the headers and footers use the same
     * elements in the client-side and it's not possible to create a
     * hierarchical DOM structure to support this case. You can, however, join
     * all three cells in the footer, because then it's again possible to
     * organize the client-side elements in a hierarchical structure.
     *
     * @param cells
     *            the cells to join
     * @return the merged cell
     * @throws IllegalArgumentException
     *             if it's not possible to join the given cells
     */
    @SuppressWarnings("unchecked")
    public CELL join(CELL... cells) {
        return join(Arrays.asList(cells));
    }

    /**
     * Replaces the given cells with a new cell that takes the full space of the
     * joined cells.
     * <p>
     * The cells to join must be adjacent cells in this row, and this row must
     * be the out-most row.
     * <p>
     * The way that the client-side web component works also causes some
     * limitations to which cells can be joined. For example, if you join the
     * first and second cell in the header, you cannot join the second and third
     * cell in the footer. This is because the headers and footers use the same
     * elements in the client-side and it's not possible to create a
     * hierarchical DOM structure to support this case. You can, however, join
     * all three cells in the footer, because then it's again possible to
     * organize the client-side elements in a hierarchical structure.
     *
     * @param cells
     *            the cells to join
     * @return the merged cell
     * @throws IllegalArgumentException
     *             if it's not possible to join the given cells
     */
    public CELL join(Collection<CELL> cells) {
        Grid<?> grid = layer.getGrid();
        if (!isOutmostRow()) {
            throw new IllegalArgumentException(
                    "Cells can be joined only on the top-most HeaderRow "
                            + "or the bottom-most FooterRow.");
        }
        if (cells.size() < 2) {
            throw new IllegalArgumentException("Cannot join less than 2 cells");
        }
        if (!this.cells.containsAll(cells)) {
            throw new IllegalArgumentException(
                    "Cannot join cells that don't belong to this row");
        }

        List<CELL> sortedCells = cells.stream().sorted((c1, c2) -> Integer
                .compare(this.cells.indexOf(c1), this.cells.indexOf(c2)))
                .collect(Collectors.toList());

        int cellInsertIndex = this.cells.indexOf(sortedCells.get(0));
        IntStream.range(0, sortedCells.size()).forEach(i -> {
            if (this.cells.indexOf(sortedCells.get(i)) != cellInsertIndex + i) {
                throw new IllegalArgumentException(
                        "Cannot join cells that are not adjacent");
            }
        });

        List<AbstractColumn<?>> columnsToJoin = sortedCells.stream()
                .map(CELL::getColumn).collect(Collectors.toList());

        List<Column<?>> bottomColumnsToJoin = columnsToJoin.stream()
                .flatMap(col -> col.getBottomChildColumns().stream())
                .collect(Collectors.toList());

        List<ColumnLayer> layers = grid.getColumnLayers();

        int layerInsertIndex = findFirstPossibleInsertIndex(bottomColumnsToJoin,
                layers);

        if (layerInsertIndex == layers.indexOf(layer) + 1) {
            /*
             * The cells can be joined in place, without re-ordering the column
             * layers
             */
            return joinCellsInPlace(cells, columnsToJoin, cellInsertIndex);
        }
        /*
         * We need to move the column layer up in the hierarchy
         */
        return moveColumnLayerAndJoinCells(cells, columnsToJoin,
                bottomColumnsToJoin, layers, layerInsertIndex, grid);
    }

    /*
     * Finds a place in the column-layers where the layer corresponding to this
     * row could be inserted with the given columns joined.
     */
    private int findFirstPossibleInsertIndex(
            List<Column<?>> bottomColumnsToJoin, List<ColumnLayer> layers) {

        for (int i = layers.indexOf(layer) + 1; i < layers.size(); i++) {
            ColumnLayer possibleParentLayer = layers.get(i);

            boolean hasCommonParentColumnForColumnsToJoin = possibleParentLayer
                    .getColumns().stream()
                    .anyMatch(column -> column.getBottomChildColumns()
                            .containsAll(bottomColumnsToJoin));
            if (hasCommonParentColumnForColumnsToJoin) {
                return i;
            }

            List<AbstractColumn<?>> joinedColumns = possibleParentLayer
                    .getColumns().stream().filter(col -> ((ColumnGroup) col)
                            .getChildColumns().size() > 1)
                    .collect(Collectors.toList());
            boolean otherColumnsJoined = joinedColumns.stream()
                    .flatMap(col -> col.getBottomChildColumns().stream())
                    .anyMatch(col -> !bottomColumnsToJoin.contains(col));
            if (otherColumnsJoined) {
                throw new IllegalArgumentException(
                        "This set of cells can not be joined because of the hierarchical "
                                + "column group structure of the client-side web component.");
            }
        }
        return layers.size();
    }

    private CELL joinCellsInPlace(Collection<CELL> cellsToJoin,
            List<AbstractColumn<?>> columnsToJoin, int cellInsertIndex) {
        Element parent = columnsToJoin.get(0).getElement().getParent();
        int elementInsertIndex = columnsToJoin.stream()
                .mapToInt(col -> parent.indexOfChild(col.getElement())).min()
                .getAsInt();
        columnsToJoin.forEach(col -> col.getElement().removeFromParent());

        List<AbstractColumn<?>> childColumns = new ArrayList<>();
        columnsToJoin.forEach(col -> childColumns
                .addAll(((ColumnGroup) col).getChildColumns()));

        ColumnGroup group = new ColumnGroup(layer.getGrid(), childColumns);

        parent.insertChild(elementInsertIndex, group.getElement());
        layer.addColumn(cellInsertIndex, group);

        layer.getColumns().removeAll(columnsToJoin);
        CELL keeper = this.cells.get(elementInsertIndex);
        this.cells.removeAll(cellsToJoin.stream().filter(cell -> cell != keeper)
                .collect(Collectors.toList()));

        return this.cells.get(cellInsertIndex);
    }

    private CELL moveColumnLayerAndJoinCells(Collection<CELL> cellsToJoin,
            List<AbstractColumn<?>> columnsToJoin,
            List<Column<?>> bottomColumnsToJoin, List<ColumnLayer> layers,
            int layerInsertIndex, Grid<?> grid) {
        grid.removeColumnLayer(layer);
        layerInsertIndex--;

        ColumnLayer lowerLayer = layers.get(layerInsertIndex - 1);
        List<AbstractColumn<?>> childColumns = lowerLayer.getColumns().stream()
                .filter(col -> bottomColumnsToJoin
                        .containsAll(col.getBottomChildColumns()))
                .collect(Collectors.toList());

        List<AbstractColumn<?>> newColumns = new ArrayList<AbstractColumn<?>>();
        Iterator<AbstractColumn<?>> leftColumns = layer.getColumns().stream()
                .filter(column -> !columnsToJoin.contains(column)).iterator();

        ArrayList<CELL> newCells = new ArrayList<>();
        Iterator<CELL> leftCells = this.cells.stream()
                .filter(cell -> !cellsToJoin.contains(cell)).iterator();

        CELL newCell = null;
        for (AbstractColumn<?> col : lowerLayer.getColumns()) {
            if (childColumns.contains(col)) {
                if (newCell == null) {
                    ColumnGroup groupForNewCell = ColumnGroupHelpers
                            .wrapInColumnGroup(grid, childColumns);
                    newColumns.add(groupForNewCell);

                    newCell = cellCtor.apply(groupForNewCell);
                    newCells.add(newCell);
                }
            } else {
                ColumnGroup group = ColumnGroupHelpers.wrapInColumnGroup(grid,
                        col);
                AbstractColumn<?> oldGroup = leftColumns.next();
                group.setHeaderRenderer(oldGroup.getHeaderRenderer());
                group.setFooterRenderer(oldGroup.getFooterRenderer());
                newColumns.add(group);

                CELL next = leftCells.next();
                next.setColumn(group);
                newCells.add(next);
            }
        }
        ColumnLayer newLayer = grid.insertColumnLayer(layerInsertIndex,
                newColumns);
        this.cells = newCells;
        if (layer.isHeaderRow()) {
            newLayer.setHeaderRow(layer.asHeaderRow());
        }
        if (layer.isFooterRow()) {
            newLayer.setFooterRow(layer.asFooterRow());
        }
        return newCell;
    }

    /**
     * Gets whether this is the top-most HeaderRow or the bottom-most FooterRow.
     *
     * @return whether this is the outmost row
     */
    protected abstract boolean isOutmostRow();
}
