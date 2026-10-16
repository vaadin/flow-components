/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderFormatting;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ComparisonOperator;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.junit.jupiter.api.Test;

/**
 * The methods an application can override to change what the component does are
 * called on every path that does what they do.
 * <p>
 * A helper that runs its own copy of such a step skips the override: an
 * override of, say, {@code deleteSheetWithPOIIndex} would be skipped when the
 * sheet is deleted through {@code deleteSheet}.
 */
class OverridableHooksTest {

    private final List<String> calls = new ArrayList<>();

    @Test
    void userAutofit_goesThroughAutofitColumn() {
        Spreadsheet spreadsheet = new Spreadsheet() {
            @Override
            public void autofitColumn(int columnIndex) {
                calls.add("autofitColumn " + columnIndex);
                super.autofitColumn(columnIndex);
            }
        };

        spreadsheet.onColumnAutofit(2);

        assertEquals(List.of("autofitColumn 2"), calls);
    }

    @Test
    void deleteSheet_goesThroughDeleteSheetWithPOIIndex() {
        Spreadsheet spreadsheet = new Spreadsheet() {
            @Override
            public void deleteSheetWithPOIIndex(int poiSheetIndex) {
                calls.add("deleteSheetWithPOIIndex " + poiSheetIndex);
                super.deleteSheetWithPOIIndex(poiSheetIndex);
            }
        };
        spreadsheet.createNewSheet("Second", 5, 5);
        calls.clear();

        spreadsheet.deleteSheet(1);

        assertEquals(List.of("deleteSheetWithPOIIndex 1"), calls);
    }

    /** A spreadsheet of two sheets, the second shown, with the calls. */
    private Spreadsheet showingTheSecondOfTwoSheets() {
        Spreadsheet spreadsheet = new Spreadsheet() {
            @Override
            public void setActiveSheetWithPOIIndex(int sheetIndex) {
                calls.add("setActiveSheetWithPOIIndex " + sheetIndex);
                super.setActiveSheetWithPOIIndex(sheetIndex);
            }
        };
        spreadsheet.createNewSheet("Second", 5, 5);
        calls.clear();
        return spreadsheet;
    }

    @Test
    void deletingTheSheetShown_showsTheNextThroughSetActiveSheetWithPOIIndex() {
        Spreadsheet spreadsheet = showingTheSecondOfTwoSheets();

        spreadsheet.deleteSheetWithPOIIndex(1);

        assertEquals(List.of("setActiveSheetWithPOIIndex 0"), calls);
    }

    @Test
    void hidingTheSheetShown_showsTheNextThroughSetActiveSheetWithPOIIndex() {
        Spreadsheet spreadsheet = showingTheSecondOfTwoSheets();

        spreadsheet.setSheetHidden(1, SheetVisibility.HIDDEN);

        assertEquals(List.of("setActiveSheetWithPOIIndex 0"), calls);
    }

    @Test
    void setActiveSheetIndex_goesThroughSetActiveSheetWithPOIIndex() {
        Spreadsheet spreadsheet = new Spreadsheet() {
            @Override
            public void setActiveSheetWithPOIIndex(int sheetIndex) {
                calls.add("setActiveSheetWithPOIIndex " + sheetIndex);
                super.setActiveSheetWithPOIIndex(sheetIndex);
            }
        };
        spreadsheet.createNewSheet("Second", 5, 5);
        calls.clear();

        spreadsheet.setActiveSheetIndex(0);

        assertEquals(List.of("setActiveSheetWithPOIIndex 0"), calls);
    }

    @Test
    void setSheetName_goesThroughSetSheetNameWithPOIIndex() {
        Spreadsheet spreadsheet = new Spreadsheet() {
            @Override
            public void setSheetNameWithPOIIndex(int sheetIndex,
                    String sheetName) {
                calls.add("setSheetNameWithPOIIndex " + sheetIndex + " "
                        + sheetName);
                super.setSheetNameWithPOIIndex(sheetIndex, sheetName);
            }
        };

        spreadsheet.setSheetName(0, "Renamed");

        assertEquals(List.of("setSheetNameWithPOIIndex 0 Renamed"), calls);
    }

    @Test
    void setActiveSheetProtected_goesThroughSetSheetProtected() {
        Spreadsheet spreadsheet = new Spreadsheet() {
            @Override
            public void setSheetProtected(int sheetPOIIndex, String password) {
                calls.add(
                        "setSheetProtected " + sheetPOIIndex + " " + password);
                super.setSheetProtected(sheetPOIIndex, password);
            }
        };

        spreadsheet.setActiveSheetProtected("secret");

        assertEquals(List.of("setSheetProtected 0 secret"), calls);
    }

    @Test
    void deleteTable_goesThroughUnregisterTable() {
        Spreadsheet spreadsheet = new Spreadsheet() {
            @Override
            public void unregisterTable(SpreadsheetTable table) {
                calls.add("unregisterTable");
                super.unregisterTable(table);
            }
        };
        SpreadsheetTable table = new SpreadsheetTable(spreadsheet,
                CellRangeAddress.valueOf("A1:B3"));
        spreadsheet.registerTable(table);

        spreadsheet.deleteTable(table);

        assertEquals(List.of("unregisterTable"), calls);
        assertTrue(spreadsheet.getTables().isEmpty());
    }

    @Test
    void conditionalFormatting_isMatchedThroughTheFormattersOverridableMethods() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Rules");
        // A formula cell, so a value rule asks the formula evaluator for it.
        sheet.createRow(0).createCell(0).setCellFormula("2+3");
        SheetConditionalFormatting formatting = sheet
                .getSheetConditionalFormatting();
        ConditionalFormattingRule valueRule = formatting
                .createConditionalFormattingRule(ComparisonOperator.GT, "1");
        valueRule.createFontFormatting().setFontStyle(true, false);
        ConditionalFormattingRule formulaRule = formatting
                .createConditionalFormattingRule("$A$1>1");
        formulaRule.createFontFormatting().setFontStyle(false, true);
        formatting.addConditionalFormatting(
                new CellRangeAddress[] { CellRangeAddress.valueOf("A1") },
                valueRule, formulaRule);

        Spreadsheet spreadsheet = new Spreadsheet(workbook) {
            @Override
            protected ConditionalFormatter createConditionalFormatter() {
                return new ConditionalFormatter(this) {
                    @Override
                    protected boolean matchesValue(Cell cell,
                            ConditionalFormattingRule rule, int deltaColumn,
                            int deltaRow) {
                        calls.add("matchesValue");
                        return super.matchesValue(cell, rule, deltaColumn,
                                deltaRow);
                    }

                    @Override
                    protected boolean matchesFormula(Cell cell,
                            ConditionalFormattingRule rule, int deltaColumn,
                            int deltaRow) {
                        calls.add("matchesFormula");
                        return super.matchesFormula(cell, rule, deltaColumn,
                                deltaRow);
                    }

                    @Override
                    protected FormulaEvaluator getFormulaEvaluator() {
                        calls.add("getFormulaEvaluator");
                        return super.getFormulaEvaluator();
                    }
                };
            }
        };
        calls.clear();

        spreadsheet.getConditionalFormatter().createConditionalFormatterRules();

        assertTrue(calls.contains("matchesValue"), calls::toString);
        assertTrue(calls.contains("matchesFormula"), calls::toString);
        assertTrue(calls.contains("getFormulaEvaluator"), calls::toString);
    }

    @Test
    void conditionalFormatting_bordersUseTheFormattersColorConverter() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Rules");
        sheet.createRow(0).createCell(0).setCellValue(5);
        SheetConditionalFormatting formatting = sheet
                .getSheetConditionalFormatting();
        ConditionalFormattingRule rule = formatting
                .createConditionalFormattingRule(ComparisonOperator.GT, "1");
        rule.createBorderFormatting().setBorderRight(BorderStyle.THIN);
        formatting.addConditionalFormatting(
                new CellRangeAddress[] { CellRangeAddress.valueOf("A1") },
                rule);

        Spreadsheet spreadsheet = new Spreadsheet(workbook) {
            @Override
            protected ConditionalFormatter createConditionalFormatter() {
                return new OwnColorConverterFormatter(this);
            }
        };
        spreadsheet.getConditionalFormatter().createConditionalFormatterRules();

        String styles = String.join("",
                spreadsheet.getConditionalFormattingStyles().values());
        assertTrue(styles.contains("border-right-color:#c0ffee;"), styles);
    }

    @Test
    void reloadingTheSheetData_findsTheConditionalFormatterOfTheNewWorkbook() {
        List<ConditionalFormatter> seen = new ArrayList<>();
        Spreadsheet spreadsheet = new Spreadsheet() {
            @Override
            protected void reloadActiveSheetData() {
                seen.add(getConditionalFormatter());
                super.reloadActiveSheetData();
            }
        };
        XSSFWorkbook next = new XSSFWorkbook();
        next.createSheet();
        seen.clear();

        spreadsheet.setWorkbook(next);

        assertSame(spreadsheet.getConditionalFormatter(), seen.get(0));
    }

    /**
     * A formatter that assigns its own colour converter, which reports every
     * border colour as {@code #c0ffee}.
     */
    private static class OwnColorConverterFormatter
            extends ConditionalFormatter {

        OwnColorConverterFormatter(Spreadsheet spreadsheet) {
            super(spreadsheet);
            colorConverter = new XSSFColorConverter(
                    (XSSFWorkbook) spreadsheet.getWorkbook()) {
                @Override
                public String getBorderColorCSS(BorderSide borderSide,
                        String attr, BorderFormatting format) {
                    return attr + ":#c0ffee;";
                }
            };
        }
    }
}
