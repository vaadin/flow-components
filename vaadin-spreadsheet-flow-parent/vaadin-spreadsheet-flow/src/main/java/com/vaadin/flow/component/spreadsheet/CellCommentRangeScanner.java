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
 * Scans a single cell range and harvests:
 * <ul>
 * <li>POI cell comments → text + author for client display, plus the set of
 * comments that are currently visible;</li>
 * <li>cells that are marked as having invalid formulas → keys for the
 * client-side error indicator.</li>
 * </ul>
 *
 * <p>
 * Skips cells inside merged regions (except the top-left anchor) and cells in
 * rows or columns hidden on the active sheet. One range per call; the
 * orchestration of viewport panes lives in {@link CellCommentLoader}.
 */
@SuppressWarnings("serial")
final class CellCommentRangeScanner implements Serializable {

    private final Spreadsheet spreadsheet;

    CellCommentRangeScanner(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    void scan(int r1, int c1, int r2, int c2) {
        Sheet sheet = spreadsheet.getActiveSheet();
        HashMap<String, String> updatedComments = new HashMap<>(
                spreadsheet.getCellComments());
        HashMap<String, String> updatedAuthors = new HashMap<>(
                spreadsheet.getCellCommentAuthors());
        ArrayList<String> updatedVisible = new ArrayList<>(
                spreadsheet.getVisibleCellComments());
        Set<String> updatedInvalidFormulas = new HashSet<>(
                spreadsheet.getInvalidFormulaCells());

        for (int r = r1 - 1; r < r2; r++) {
            Row row = sheet.getRow(r);
            if (row != null && row.getZeroHeight()) {
                continue;
            }
            for (int c = c1 - 1; c < c2; c++) {
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
                        updatedComments.put(key,
                                comment.getString().getString());
                        updatedAuthors.put(key, comment.getAuthor());
                        if (comment.isVisible()) {
                            updatedVisible.add(key);
                        }
                    }
                    if (spreadsheet.isMarkedAsInvalidFormula(c_one_based,
                            row_one_based)) {
                        updatedInvalidFormulas.add(key);
                    }
                } else {
                    c = region.col2 - 1;
                }
            }
        }
        spreadsheet.setCellComments(updatedComments);
        spreadsheet.setCellCommentAuthors(updatedAuthors);
        spreadsheet.setVisibleCellComments(updatedVisible);
        spreadsheet.setInvalidFormulaCells(updatedInvalidFormulas);
    }
}
