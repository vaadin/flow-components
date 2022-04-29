package com.vaadin.flow.component.spreadsheet.command;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

/**
 * Command for changing the height of row(s) or the width of column(s).
 *
 * @author Vaadin Ltd.
 * @since 1.0
 */
public class SizeChangeCommand extends SpreadsheetCommand {

    /**
     * Determines whether this command applies to a row or to a column.
     */
    public enum Type {
        COLUMN, ROW
    };

    private final Type type;
    private Object[] values;
    private Integer[] indexes;

    public SizeChangeCommand(Spreadsheet spreadsheet, Type type) {
        super(spreadsheet);
        this.type = type;
    }

    /**
     * Returns the type of size change this represents.
     *
     * @return size change type
     */
    public Type getType() {
        return type;
    }

    /**
     * Captures the current row heights or column widths (depending on the type
     * set to this command) for the row/column indexes given.
     *
     * @param indexes
     *            Row /column indexes, 1-based
     */
    public void captureValues(Integer[] indexes) {
        this.indexes = indexes;
        values = new Object[indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            values[i] = getCurrentValue(indexes[i] - 1);
        }
    }

    @Override
    public void execute() {
        for (int i = 0; i < indexes.length; i++) {
            values[i] = updateValue(indexes[i] - 1, values[i]);
        }
    }

    @Override
    public CellReference getSelectedCellReference() {
        return null;
    }

    @Override
    public CellRangeAddress getPaintedCellRange() {
        return null;
    }

    /**
     * Sets the height/width of the target row/column (found by the given index)
     * to the given value.
     *
     * @param index
     *            row/column index, 0-based
     * @param value
     *            new height/width
     * @return Previous height/width of the row/column
     */
    private Object updateValue(int index, Object value) {
        if (type == Type.COLUMN) {
            Object columnWidth = getCurrentValue(index);
            spreadsheet.setColumnWidth(index, (Integer) value);
            return columnWidth;
        } else if (type == Type.ROW) {
            Row row = spreadsheet.getActiveSheet().getRow(index);
            // null rows use default row height
            // null height marks default height
            Object oldHeight = getCurrentValue(index);

            if (value == null && row != null) {
                spreadsheet.setRowHeight(index,
                        spreadsheet.getDefaultRowHeight());
            } else if (value != null) {
                spreadsheet.setRowHeight(index, (Float) value);
            } // if both are null, then default is applied already (shouldn't)
            return oldHeight;
        }
        return null;
    }

    /**
     * Returns the current height/width of the target row/column.
     *
     * @param index
     *            row/column index, 0-based
     * @return current height for row OR width for column
     */
    private Object getCurrentValue(int index) {
        if (type == Type.COLUMN) {
            if (getSheet().isColumnHidden(index)) {
                return 0;
            } else {
                return getSheet().getColumnWidthInPixels(index);
            }
        } else if (type == Type.ROW) {
            Row row = getSheet().getRow(index);
            // null rows use default row height
            // null height marks default height
            return row == null ? null
                    : row.getZeroHeight() ? 0.0F : row.getHeightInPoints();
        }
        return null;
    }

}
