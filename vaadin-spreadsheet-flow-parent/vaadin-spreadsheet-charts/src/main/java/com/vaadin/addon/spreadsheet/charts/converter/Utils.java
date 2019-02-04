package com.vaadin.addon.spreadsheet.charts.converter;

/*
 * #%L
 * Vaadin Spreadsheet Charts Integration
 * %%
 * Copyright (C) 2016 Vaadin Ltd
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.addon.spreadsheet.Spreadsheet;

public class Utils {
    private static final Logger LOGGER = Logger
            .getLogger(Utils.class.getName());

    @SuppressWarnings("unchecked")
    public static <T> T callMethodUsingReflection(Object o, String name) {
        try {
            Method method = o.getClass().getMethod(name);
            return (T) method.invoke(o);
        } catch (Exception e) {
            // this should never happen
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <E extends Enum> E getEnumValueOrDefault(
            Class<? extends E> eClass, String value, E defaultValue) {
        try {
            return (E) Enum.valueOf(eClass, value);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    public static String getStringValueFromFormula(String formula,
            Spreadsheet spreadsheet) {
        List<String> strings = new ArrayList<String>();

        for (CellReference ref : getAllReferencedCells(spreadsheet.getWorkbook().getSpreadsheetVersion(), formula)) {
            strings.add(getStringValue(ref, spreadsheet));
        }

        return join(strings, " ");
    }

    public static String join(final List<String> array,
            final String separator) {
        final StringBuilder buf = new StringBuilder(array.size() * 16);
        for (String str : array) {
            buf.append(str);
            buf.append(separator);
        }
        buf.setLength(buf.length() - separator.length());
        return buf.toString();
    }

    /**
     * @param version for inferring ranges for column-only references
     * @param formula
     * @return all cells in the referenced areas
     */
    public static List<CellReference> getAllReferencedCells(SpreadsheetVersion version, String formula) {
        ArrayList<CellReference> cellRefs = new ArrayList<CellReference>();
        for (AreaReference area : getAreaReferences(version, formula)) {
            cellRefs.addAll(Arrays.asList(area.getAllReferencedCells()));
        }
        return cellRefs;
    }

    /**
     * Returns an array of contiguous area references addressed by the given
     * formula.
     * @param version to infer max # of rows for column-only formula references
     * @param formula containing possibly non-contiguous area refrences
     * @return array of references
     */
    public static AreaReference[] getAreaReferences(SpreadsheetVersion version, String formula) {
        String formulaIn = formula;
        // generateContiguous cannot parse a formula in parentheses
        if (formulaIn.startsWith("(") && formulaIn.endsWith(")")) {
            formulaIn = formulaIn.substring(1, formulaIn.length() - 1);
        }

        return AreaReference.generateContiguous(version, formulaIn);
    }

    /**
     * This function uses the getAllReferencedCells function but filters out all
     * the hidden rows from the list honoring filtering of charts based on
     * spreadsheettable filter settings
     */
    public static List<CellReference> getAllReferencedVisibleCells(
            String formula, Spreadsheet spreadsheet) {
        return getAllReferencedCells(formula, spreadsheet, false);
    }

    /**
     * This function returns all the cells that the given formula references.
     * You can optionally filter out all the hidden rows from the list honoring
     * filtering of charts based on spreadsheettable filter settings.
     * 
     * @param formula
     *            The formula to find referenced cells for
     * @param spreadsheet
     *            Spreadsheet to operate on
     * @param includeHiddenCells
     *            <code>true</code> to include cells residing on hidden rows or
     *            columns, <code>false</code> to omit them
     * 
     */
    public static List<CellReference> getAllReferencedCells(String formula,
            Spreadsheet spreadsheet, boolean includeHiddenCells) {
        final List<CellReference> cellRefs = getAllReferencedCells(spreadsheet.getWorkbook().getSpreadsheetVersion(), formula);

        if (includeHiddenCells) {
            return cellRefs;
        } else {
            // Filter out hidden cells of rows that are hidden (Excel spec)
            ArrayList<CellReference> visibleCells = new ArrayList<CellReference>();
            for (CellReference cr : cellRefs) {
                if (!spreadsheet.isRowHidden(cr.getRow())
                        && !spreadsheet.isColumnHidden(cr.getCol())) {
                    visibleCells.add(cr);
                }
            }
            return visibleCells;
        }
    }

    public static String getStringValue(CellReference ref,
            Spreadsheet spreadsheet) {
        Sheet sheet = spreadsheet.getWorkbook().getSheet(ref.getSheetName());
        return spreadsheet.getCellValue(spreadsheet.getCell(ref, sheet));
    }

    public static Double getNumericValue(CellReference ref,
            Spreadsheet spreadsheet) {
        try {
            Sheet sheet = spreadsheet.getWorkbook()
                    .getSheet(ref.getSheetName());
            Cell cell = spreadsheet.getCell(ref, sheet);
            spreadsheet.getFormulaEvaluator().evaluateFormulaCell(cell);

            if (cell.getCellType() == CellType.NUMERIC
                    || cell.getCellType() == CellType.FORMULA) {
                return cell.getNumericCellValue();
            }
        } catch (NullPointerException e) {
        } catch (IllegalStateException e) {
        } catch (NumberFormatException e) {

        } catch (FormulaParseException e) {
            logError();
        }
        return null;
    }

    private static void logError() {
        final String ERROR_TEXT = "The format of this data series is not supported by Vaadin Spreadsheet. "
                + "Please see our list "
                + "of known limitations: https://vaadin.com/docs/-/part/spreadsheet/spreadsheet-overview.html limitations.";
        LOGGER.warning(ERROR_TEXT);
    }
}
