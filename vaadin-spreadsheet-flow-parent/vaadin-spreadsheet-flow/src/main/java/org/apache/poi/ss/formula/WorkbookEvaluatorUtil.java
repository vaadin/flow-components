/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package org.apache.poi.ss.formula;

import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.BaseXSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.BaseXSSFFormulaEvaluator;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

/**
 * Helper to evaluate POI values that are package scoped, like formula Ptg[]
 * arrays.
 */
public class WorkbookEvaluatorUtil {

    /**
     * Evaluate formula Ptg[] tokens
     */
    public static ValueEval evaluate(Spreadsheet spreadsheet, Ptg[] ptgs,
            Cell cell) {
        // allow for reuse of evaluation caches for performance - see POI #57840
        // for an example
        final WorkbookEvaluator workbookEvaluator = ((BaseXSSFFormulaEvaluator) spreadsheet
                .getFormulaEvaluator())._getWorkbookEvaluator();
        final OperationEvaluationContext ec = new OperationEvaluationContext(
                workbookEvaluator, workbookEvaluator.getWorkbook(),
                getSheetIndex(cell), cell.getRowIndex(), cell.getColumnIndex(),
                new EvaluationTracker(new EvaluationCache(null)));
        return workbookEvaluator.evaluateFormula(ec, ptgs);
    }

    public static BaseXSSFEvaluationWorkbook getEvaluationWorkbook(
            Spreadsheet spreadsheet) {
        return (BaseXSSFEvaluationWorkbook) ((BaseXSSFFormulaEvaluator) spreadsheet
                .getFormulaEvaluator())._getWorkbookEvaluator().getWorkbook();
    }

    private static int getSheetIndex(Cell cell) {
        Sheet sheet = cell.getSheet();
        return sheet.getWorkbook().getSheetIndex(sheet);
    }
}
