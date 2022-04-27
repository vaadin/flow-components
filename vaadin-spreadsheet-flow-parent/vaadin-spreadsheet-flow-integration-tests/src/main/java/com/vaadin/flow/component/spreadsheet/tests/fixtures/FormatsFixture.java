package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.Calendar;
import java.util.Date;

public class FormatsFixture implements SpreadsheetFixture {
    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        Cell c;

        /*
         * 0 General General 18 Time h:mm AM/PM 1 Decimal 0 19 Time h:mm:ss
         * AM/PM 2 Decimal 0.00 20 Time h:mm 3 Decimal #,##0 21 Time h:mm:ss 4
         * Decimal #,##0.00 2232 Date/Time M/D/YY h:mm 531 Currency
         * "$"#,##0_);("$"#,##0) 37 Account. _(#,##0_);(#,##0) 631 Currency
         * "$"#,##0_);[Red]("$"#,##0) 38 Account. _(#,##0_);[Red](#,##0) 731
         * Currency "$"#,##0.00_);("$"#,##0.00) 39 Account.
         * _(#,##0.00_);(#,##0.00) 831 Currency "$"#,##0.00_);[Red]("$"#,##0.00)
         * 40 Account. _(#,##0.00_);[Red](#,##0.00) 9 Percent 0% 4131 Currency
         * _("$"* #,##0_);_("$"* (#,##0);_("$"* "-"_);_(@_) 10 Percent 0.00%
         * 4231 33 Currency _(* #,##0_);_(* (#,##0);_(* "-"_);_(@_) 11
         * Scientific 0.00E+00 4331 Currency _("$"* #,##0.00_);_("$"*
         * (#,##0.00);_("$"* "-"??_);_(@_) 12 Fraction # ?/? 4431 33 Currency
         * _(* #,##0.00_);_(* (#,##0.00);_(* "-"??_);_(@_) 13 Fraction # ??/??
         * 45 Time mm:ss 1432 Date M/D/YY 46 Time [h]:mm:ss 15 Date D-MMM-YY 47
         * Time mm:ss.0 16 Date D-MMM 48 Scientific ##0.0E+0 17 Date MMM-YY 49
         * Text @
         */

        spreadsheet.createCell(0, 0, "JType/Style");
        spreadsheet.createCell(1, 0, "String");
        spreadsheet.createCell(2, 0, "Date");
        spreadsheet.createCell(3, 0, "Boolean");
        spreadsheet.createCell(4, 0, "Calendar");
        c = spreadsheet.createCell(5, 0, "Double");
        c = spreadsheet.createCell(6, 0, "String");
        Workbook wb = c.getSheet().getWorkbook();

        short[] formats = new short[] { 0, 2, 10, 15, 11, 49 };
        String[] stylesStr = new String[] { "General", "0.00", "Percent",
                "D-MMM-YY", "0.00E+00", "Text" };
        int column = 0;
        for (String title : stylesStr) {
            column++;
            c = spreadsheet.createCell(0, column, title);
        }

        spreadsheet.createCell(0, formats.length + 2, "Cell Types");
        spreadsheet.createCell(0, formats.length + 3, "0 = NUMERIC");
        spreadsheet.createCell(0, formats.length + 4, "1 = STRING");
        spreadsheet.createCell(0, formats.length + 5, "2 = FORMULA");
        spreadsheet.createCell(0, formats.length + 6, "3 = BLANK");
        spreadsheet.createCell(0, formats.length + 7, "4 = BOOLEAN");

        column = 0;
        for (short format : formats) {
            CellStyle style = wb.createCellStyle();
            style.setDataFormat(format);

            column++;
            c = spreadsheet.createCell(1, column, "example");
            c.setCellStyle(style);
            spreadsheet.createCell(1, column + formats.length + 1,
                    "" + c.getCellType().ordinal());

            c = spreadsheet.createCell(2, column, new Date(1095379000000l));
            c.setCellStyle(style);
            spreadsheet.createCell(2, column + formats.length + 1,
                    "" + c.getCellType().ordinal());

            c = spreadsheet.createCell(3, column, Boolean.TRUE);
            c.setCellStyle(style);
            spreadsheet.createCell(3, column + formats.length + 1,
                    "" + c.getCellType().ordinal());

            c = spreadsheet.createCell(4, column, Calendar.getInstance());
            c.setCellStyle(style);
            spreadsheet.createCell(4, column + formats.length + 1,
                    "" + c.getCellType().ordinal());

            c = spreadsheet.createCell(5, column, Double.parseDouble("3.1415"));
            c.setCellStyle(style);
            spreadsheet.createCell(5, column + formats.length + 1,
                    "" + c.getCellType().ordinal());

            c = spreadsheet.createCell(6, column, "3.1415");
            c.setCellStyle(style);
            spreadsheet.createCell(6, column + formats.length + 1,
                    "" + c.getCellType().ordinal());
        }

        int formulaBaseColumn = 0;
        int formulaBaseRow = 8;

        c = spreadsheet.createCell(formulaBaseRow, formulaBaseColumn,
                "Furmulas");
        c = spreadsheet.createCell(formulaBaseRow, formulaBaseColumn + 1,
                "Result");
        c = spreadsheet.createCell(formulaBaseRow, formulaBaseColumn + 2,
                "CellType");
        String[] formulas = new String[] { "B1+C1", "B4+C4", "B7+C7", "B6+C6",
                "SUM(B6:D6)", "SUM(B7:D7)", "SUM(E5:E6)", "E5+E6" };

        spreadsheet.refreshAllCellValues();
        int row = formulaBaseRow;
        for (String formula : formulas) {
            row++;
            c = spreadsheet.createCell(row, formulaBaseColumn, formula);
            c = spreadsheet.createCell(row, formulaBaseColumn + 1, "");
            // sheetController.setCellType(Cell.CELL_TYPE_FORMULA);
            c.setCellFormula(formula);
            c = spreadsheet.createCell(row, formulaBaseColumn + 2,
                    c.getCellType().ordinal());
        }

        spreadsheet.refreshAllCellValues();
    }
}