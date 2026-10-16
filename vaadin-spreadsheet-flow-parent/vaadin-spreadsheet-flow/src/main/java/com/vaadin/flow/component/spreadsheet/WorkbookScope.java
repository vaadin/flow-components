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
import java.util.Map;

import org.apache.poi.ss.formula.BaseFormulaEvaluator;
import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

/**
 * What lives as long as one workbook is shown, and only as long: made anew for
 * each workbook rather than cleared, so nothing of the last one is left over,
 * whatever is added here later.
 * <p>
 * The style factory and the conditional formatter are made through the methods
 * an application may override, once the scope is in place for them to read.
 */
final class WorkbookScope implements Serializable {

    final FormulaEvaluator formulaEvaluator;
    final ConditionalFormattingEvaluator formattingEvaluator;
    final TableRegistry tables = new TableRegistry();
    final SheetScrollMemory scrollMemory;
    final SheetState sheetState;
    final InvalidFormulaTracker invalidFormulas;
    final PopupButtonRegistry popupButtons;
    /** How wide a character is in each cell style, as the client measures. */
    final CellFitCalculator fitCalculator = new CellFitCalculator();
    /**
     * The widths the columns were autofitted to, by sheet and column, for a
     * filter put over them to autofit them again.
     */
    final Map<CellReference, Integer> autofittedWidths = new HashMap<>();
    SpreadsheetStyleFactory styler;
    ConditionalFormatter conditionalFormatter;

    WorkbookScope(Spreadsheet spreadsheet, Workbook workbook) {
        formulaEvaluator = workbook.getCreationHelper()
                .createFormulaEvaluator();
        // Every formula evaluator of POI extends BaseFormulaEvaluator.
        formattingEvaluator = new ConditionalFormattingEvaluator(workbook,
                (BaseFormulaEvaluator) formulaEvaluator);
        scrollMemory = new SheetScrollMemory(spreadsheet);
        sheetState = new SheetState(spreadsheet);
        invalidFormulas = new InvalidFormulaTracker(spreadsheet);
        popupButtons = new PopupButtonRegistry(spreadsheet);
    }

    /**
     * The workbook is no longer shown: what the client has of it is taken off.
     */
    void close() {
        popupButtons.detachAll();
    }
}
