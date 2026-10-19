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
 * Gives the client the tooltips of the links in the cells shown. Two kinds of
 * cell have one: a cell with a {@code Hyperlink} attached, whose tooltip is the
 * link's tooltip or else its address, and a cell with a {@code HYPERLINK(...)}
 * formula, whose tooltip is where the {@link HyperlinkCellClickHandler} takes
 * it.
 */
final class HyperlinkLoader implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory
            .getLogger(HyperlinkLoader.class);

    private final Spreadsheet spreadsheet;

    HyperlinkLoader(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    void load() {
        HashMap<String, String> tooltips = new HashMap<>();
        HyperlinkCellClickHandler handler = spreadsheet
                .getHyperlinkCellClickHandler();
        spreadsheet.viewport().forEachPane(
                (r1, c1, r2, c2) -> scan(tooltips, handler, r1, c1, r2, c2));
        spreadsheet.sheetModel().hyperlinksTooltips.set(tooltips);
    }

    private void scan(HashMap<String, String> tooltips,
            HyperlinkCellClickHandler handler, int r1, int c1, int r2, int c2) {
        for (int r = r1; r <= r2; r++) {
            final Row row = spreadsheet.getActiveSheet().getRow(r);
            if (row == null) {
                continue;
            }
            for (int c = c1; c <= c2; c++) {
                Cell cell = row.getCell(c);
                if (cell == null) {
                    continue;
                }
                try {
                    Hyperlink link = cell.getHyperlink();
                    if (link != null) {
                        String tooltip;
                        if (link instanceof XSSFHyperlink xssfLink) {
                            tooltip = xssfLink.getTooltip();
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
    }
}
