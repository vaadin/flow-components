/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;

import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;

import com.vaadin.flow.component.spreadsheet.client.OverlayInfo;

/**
 * Builds the client-side {@link OverlayInfo} DTO from a
 * {@link SheetOverlayWrapper}. Handles the {@code DONT_MOVE_AND_RESIZE} XSSF
 * anchor (absolute pixel/EMU coordinates) and the regular cell-anchored case
 * (skip leading hidden rows and columns, compute width and height from the
 * active sheet's dimensions).
 */
@SuppressWarnings("serial")
final class SheetOverlayInfoBuilder implements Serializable {

    private final Spreadsheet spreadsheet;

    SheetOverlayInfoBuilder(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    OverlayInfo build(SheetOverlayWrapper overlayWrapper) {
        OverlayInfo info = new OverlayInfo(overlayWrapper.getType());
        Sheet sheet = spreadsheet.getActiveSheet();
        var anchor = overlayWrapper.getAnchor();

        // Need special handling for XSSFClientAnchor anchors of type
        // DONT_MOVE_AND_RESIZE.
        // See https://github.com/vaadin/flow-components/issues/3261
        if (AnchorType.DONT_MOVE_AND_RESIZE.equals(anchor.getAnchorType())
                && anchor instanceof XSSFClientAnchor) {
            info.col = 1;
            info.row = 1;
            var xssfAnchor = (XSSFClientAnchor) anchor;
            info.dx = (Long) xssfAnchor.getPosition().getX()
                    / Units.EMU_PER_PIXEL;
            info.dy = (Long) xssfAnchor.getPosition().getY()
                    / Units.EMU_PER_POINT;
            info.width = xssfAnchor.getSize().getCx() / Units.EMU_PER_PIXEL;
            info.height = xssfAnchor.getSize().getCy() / Units.EMU_PER_POINT;
            return info;
        }

        int col = anchor.getCol1();
        while (spreadsheet.isColumnHidden(col)) {
            col++;
        }
        int row = anchor.getRow1();
        while (spreadsheet.isRowHidden(row)) {
            row++;
        }

        info.col = col + 1; // 1-based
        info.row = row + 1; // 1-based

        info.height = overlayWrapper.getHeight(sheet, spreadsheet.getRowH());
        info.width = overlayWrapper.getWidth(sheet, spreadsheet.getColW(),
                spreadsheet.getDefColW());

        // FIXME: height and width can be -1, it is never handled anywhere

        // if original start row/column is hidden, use 0 dy/dx
        if (col == anchor.getCol1()) {
            info.dx = overlayWrapper.getDx1(sheet);
        }
        if (row == anchor.getRow1()) {
            info.dy = overlayWrapper.getDy1(sheet);
        }

        return info;
    }
}
