package com.vaadin.addon.spreadsheet.command;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2015 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeUtil;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.SpreadsheetUtil;

/**
 * Command for shifting the value(s) of one or more cells.
 * 
 * @author Vaadin Ltd.
 * @since 1.0
 */
public class CellShiftValuesCommand extends CellValueCommand {

    private boolean undone;
    private boolean decrease;

    /**
     * Creates a new CellShiftValuesCommand targeting the given spreadsheet.
     * 
     * @param spreadsheet
     *            Target spreadsheet
     * @param decrease
     */
    public CellShiftValuesCommand(Spreadsheet spreadsheet, boolean decrease) {
        super(spreadsheet);
        this.decrease = decrease;
    }

    @Override
    public void execute() {
        super.execute();
        undone = !undone;
    }

    @Override
    public CellReference getSelectedCellReference() {
        CellReference selectedCellReference = super.getSelectedCellReference();
        CellRangeAddress paintedCellRange = getPaintedCellRange();
        if (paintedCellRange == null
                || SpreadsheetUtil.isCellInRange(selectedCellReference,
                        paintedCellRange)) {
            return selectedCellReference;
        } else {
            return new CellReference(paintedCellRange.getFirstRow(),
                    paintedCellRange.getFirstColumn());
        }
    }

    @Override
    public CellRangeAddress getPaintedCellRange() {
        CellRangeAddress paintedCellRange = super.getPaintedCellRange();
        if (undone || paintedCellRange == null) {
            return paintedCellRange;
        } else {
            CellRangeValue crv = (CellRangeValue) values.get(0);
            if (decrease) {
                int col2 = crv.col1 == paintedCellRange.getFirstColumn() ? crv.col2
                        : crv.col1 - 1;
                int row2 = crv.row1 == paintedCellRange.getFirstRow() ? crv.row2
                        : crv.row1 - 1;
                return new CellRangeAddress(paintedCellRange.getFirstRow(),
                        row2, paintedCellRange.getFirstColumn(), col2);
            } else {
                return CellRangeUtil.mergeCellRanges(new CellRangeAddress[] {
                        paintedCellRange,
                        new CellRangeAddress(crv.row1, crv.row2, crv.col1,
                                crv.col2) })[0];
            }
        }
    }
}
