package com.vaadin.addon.spreadsheet.command;

import org.apache.poi.hssf.converter.ExcelToHtmlUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.addon.spreadsheet.Spreadsheet;

public class SizeChangeCommand extends SpreadsheetCommand {

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
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * 
     * @param indexes
     *            1-based
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
            values[i] = updateValue(indexes[i]-1, values[i]);
        }
    }

    /**
     * 
     * @param index
     *            0-based
     * @param value
     * @return
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
                        spreadsheet.getDefaultRowHeightInPoints());
            } else if (value != null) {
                spreadsheet.setRowHeight(index, (Float) value);
            } // if both are null, then default is applied already (shouldn't)
            return oldHeight;
        }
        return null;
    }

    /**
     * 
     * @param index
     *            0-based
     * @return
     */
    private Object getCurrentValue(int index) {
        if (type == Type.COLUMN) {
            if (getSheet().isColumnHidden(index)) {
                return 0;
            } else {
                return ExcelToHtmlUtils.getColumnWidthInPx(getSheet()
                        .getColumnWidth(index));
            }
        } else if (type == Type.ROW) {
            Row row = getSheet().getRow(index);
            // null rows use default row height
            // null height marks default height
            return row == null ? null : row.getZeroHeight() ? 0.0F : row
                    .getHeightInPoints();
        }
        return null;
    }

    @Override
    public CellReference getSelectedCellReference() {
        return null;
    }

    @Override
    public CellRangeAddress getPaintedCellRange() {
        return null;
    }

}
