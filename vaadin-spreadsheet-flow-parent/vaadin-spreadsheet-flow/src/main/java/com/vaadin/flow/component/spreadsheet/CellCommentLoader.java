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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;

import com.vaadin.flow.component.spreadsheet.client.MergedRegion;

/**
 * Gives the client the comments of the cells shown, with their authors and
 * whether they are shown always, and which of the cells have invalid formulas.
 * Hidden rows and columns are left out, and so are the cells a merged cell
 * covers but its first.
 */
final class CellCommentLoader implements Serializable {

    private final Spreadsheet spreadsheet;

    CellCommentLoader(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    void load() {
        if (!spreadsheet.viewport().isReported()) {
            // Spreadsheet not loaded. This method will be called again.
            return;
        }
        HashMap<String, String> comments = new HashMap<>();
        HashMap<String, String> authors = new HashMap<>();
        ArrayList<String> visible = new ArrayList<>();
        Set<String> invalidFormulas = new HashSet<>();
        spreadsheet.viewport().forEachPane((r1, c1, r2, c2) -> scan(comments,
                authors, visible, invalidFormulas, r1, c1, r2, c2));
        spreadsheet.sheetModel().cellComments.set(comments);
        spreadsheet.sheetModel().cellCommentAuthors.set(authors);
        spreadsheet.sheetModel().visibleCellComments.set(visible);
        spreadsheet.sheetModel().invalidFormulaCells.set(invalidFormulas);
    }

    private void scan(HashMap<String, String> comments,
            HashMap<String, String> authors, ArrayList<String> visible,
            Set<String> invalidFormulas, int r1, int c1, int r2, int c2) {
        Sheet sheet = spreadsheet.getActiveSheet();
        for (int r = r1; r <= r2; r++) {
            Row row = sheet.getRow(r);
            if (row != null && row.getZeroHeight()) {
                continue;
            }
            for (int c = c1; c <= c2; c++) {
                if (sheet.isColumnHidden(c)) {
                    continue;
                }
                int c_one_based = c + 1;
                int row_one_based = r + 1;

                MergedRegion region = spreadsheet.getMergedRegionContainer()
                        .getMergedRegion(c_one_based, row_one_based);
                // do not add comments that are "below" merged regions.
                // client side handles cases where comment "moves" (because
                // shifting etc.) from merged cell into basic or vice versa.
                if (region == null || (region.col1 == c_one_based
                        && region.row1 == row_one_based)) {
                    Comment comment = sheet
                            .getCellComment(new CellAddress(r, c));
                    String key = SpreadsheetUtil.toKey(c_one_based,
                            row_one_based);
                    if (comment != null) {
                        comments.put(key, comment.getString().getString());
                        authors.put(key, comment.getAuthor());
                        if (comment.isVisible()) {
                            visible.add(key);
                        }
                    }
                    if (spreadsheet.isMarkedAsInvalidFormula(c_one_based,
                            row_one_based)) {
                        invalidFormulas.add(key);
                    }
                }
            }
        }
    }
}
