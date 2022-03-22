package com.vaadin.addon.spreadsheet.client;

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

import com.vaadin.addon.spreadsheet.client.CopyPasteTextBox.CopyPasteHandler;

/**
 * The class that binds the {@link CopyPasteTextBox} and
 * {@link SpreadsheetWidget} together; copy operations are put into this class,
 * because we have the necessary info available. paste is delegated to
 * server-side.
 * 
 * @author Thomas Mattsson / Vaadin Ltd.
 *
 */
public class CopyPasteHandlerImpl implements CopyPasteHandler {

    private SheetWidget sheetWidget;

    public CopyPasteHandlerImpl(SheetWidget sheetWidget) {
        this.sheetWidget = sheetWidget;
    }

    @Override
    public void onPaste(String text) {
        sheetWidget.getSheetHandler().onSheetPaste(text);
    }

    @Override
    public void onCut() {
        if (sheetWidget.isCoherentSelection()) {
            sheetWidget.clearSelectedCellsOnCut();
        }
    }

    @Override
    public void onCopy() {
        // NO-OP; TODO should we notify server-side?
    }

    @Override
    public String getClipboardText() {

        if (sheetWidget.isCoherentSelection()) {
            int xMax = sheetWidget.getSelectionRightCol();
            int xMin = sheetWidget.getSelectionLeftCol();
            int yMin = sheetWidget.getSelectionTopRow();
            int yMax = sheetWidget.getSelectionBottomRow();

            StringBuilder sb = new StringBuilder();

            for (int row = yMin; row <= yMax; row++) {
                for (int col = xMin; col <= xMax; col++) {
                    String cellValue = sheetWidget.getCellValue(col, row);
                    if (cellValue != null) {
                        sb.append(cellValue);
                    }
                    if (col != xMax)
                        sb.append("\t");
                }

                if (row != yMax)
                    sb.append("\n");
            }

            String result = sb.toString();
            return result;
        }
        return "non-continous selection, can't copy";
    }

}