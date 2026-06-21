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
import java.util.HashMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.xmlbeans.impl.values.XmlValueDisconnectedException;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.spreadsheet.Spreadsheet.HyperlinkCellClickHandler;

/**
 * Scans a single cell range and harvests tooltips for any hyperlink cells. Two
 * kinds of cell are checked: cells with a direct {@code Hyperlink} attached
 * (tooltip from the link's tooltip text, falling back to its address), and
 * cells with a {@code HYPERLINK(...)} formula (tooltip resolved through the
 * active {@link HyperlinkCellClickHandler}). XSSF and HSSF anchors are both
 * supported.
 */
final class HyperlinkRangeScanner implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory
            .getLogger(HyperlinkRangeScanner.class);

    private final Spreadsheet spreadsheet;

    HyperlinkRangeScanner(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    void scan(int r1, int c1, int r2, int c2) {
        HashMap<String, String> tooltips = spreadsheet.getHyperlinksTooltips();
        HyperlinkCellClickHandler handler = spreadsheet
                .getHyperlinkCellClickHandler();
        for (int r = r1 - 1; r < r2; r++) {
            final Row row = spreadsheet.getActiveSheet().getRow(r);
            if (row == null) {
                continue;
            }
            for (int c = c1 - 1; c < c2; c++) {
                Cell cell = row.getCell(c);
                if (cell == null) {
                    continue;
                }
                try {
                    Hyperlink link = cell.getHyperlink();
                    if (link != null) {
                        String tooltip;
                        if (link instanceof XSSFHyperlink) {
                            tooltip = ((XSSFHyperlink) link).getTooltip();
                            // Show address if no defined tooltip (like in
                            // Excel)
                            if (tooltip == null) {
                                tooltip = link.getAddress();
                            }
                        } else {
                            tooltip = link.getAddress();
                        }
                        tooltips.put(SpreadsheetUtil.toKey(c + 1, r + 1),
                                tooltip);
                    } else if (DefaultHyperlinkCellClickHandler
                            .isHyperlinkFormulaCell(cell) && handler != null) {
                        tooltips.put(SpreadsheetUtil.toKey(c + 1, r + 1),
                                handler.getHyperlinkFunctionTarget(cell));
                    }
                } catch (XmlValueDisconnectedException exc) {
                    LOGGER.trace(exc.getMessage(), exc);
                }
            }
        }
        spreadsheet.setHyperlinksTooltips(tooltips);
    }
}
