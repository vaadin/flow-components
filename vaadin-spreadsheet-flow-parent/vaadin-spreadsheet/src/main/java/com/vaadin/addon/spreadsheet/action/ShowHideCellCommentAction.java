package com.vaadin.addon.spreadsheet.action;

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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.Spreadsheet.SelectionChangeEvent;

/**
 * Spreadsheet action for showing or hiding a cell comment.
 *
 * @author Vaadin Ltd.
 * @since 1.0
 */
@SuppressWarnings("serial")
public class ShowHideCellCommentAction extends SpreadsheetAction {

    public ShowHideCellCommentAction() {
        super("");
    }

    @Override
    public boolean isApplicableForSelection(Spreadsheet spreadsheet,
                                            SelectionChangeEvent event) {
        if (!spreadsheet.getActiveSheet().getProtect()) {
            if (event.getCellRangeAddresses().size() == 0
                    && event.getIndividualSelectedCells().size() == 0) {
                CellReference cr = event.getSelectedCellReference();
                Comment cellComment = spreadsheet.getActiveSheet()
                        .getCellComment(cr.getRow(), cr.getCol());
                if (cellComment != null) {
                    if (cellComment.isVisible()) {
                        setCaption("Hide comment");
                    } else {
                        setCaption("Show comment");
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isApplicableForHeader(Spreadsheet spreadsheet,
                                         CellRangeAddress headerRange) {
        return false;
    }

    @Override
    public void executeActionOnSelection(Spreadsheet spreadsheet,
                                         SelectionChangeEvent event) {
        CellReference cr = event.getSelectedCellReference();
        Comment cellComment = spreadsheet.getActiveSheet().getCellComment(
                cr.getRow(), cr.getCol());
        cellComment.setVisible(!cellComment.isVisible());
        Sheet sheet = spreadsheet.getActiveSheet();
        Row row = sheet.getRow(cr.getRow());
        if (row == null) {
            row = sheet.createRow(cr.getRow());
        }
        Cell cell = spreadsheet.getCell(cr);
        if (cell == null) {
            cell = row.createCell(cr.getCol());
        }
        spreadsheet.refreshCells(cell);
    }

    @Override
    public void executeActionOnHeader(Spreadsheet spreadsheet,
                                      CellRangeAddress headerRange) {
        throw new UnsupportedOperationException(
                "Hide header action can't be executed against a selection.");
    }

}
