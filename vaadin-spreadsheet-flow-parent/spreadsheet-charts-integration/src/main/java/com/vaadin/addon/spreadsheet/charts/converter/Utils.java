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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.addon.spreadsheet.Spreadsheet;

public class Utils {
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
    };

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

        for (CellReference ref : getAllReferencedCells(formula)) {
            strings.add(getStringValue(ref, spreadsheet));
        }

        return join(strings, " ");
    }

    public static String join(final List<String> array, final String separator) {
        final StringBuilder buf = new StringBuilder(array.size() * 16);
        for (String str : array) {
            buf.append(str);
            buf.append(separator);
        }
        buf.setLength(buf.length() - separator.length());
        return buf.toString();
    }

    public static List<CellReference> getAllReferencedCells(String formula) {

        // generateContiguous cannot parse a forumula in parentheses.
        if (formula.startsWith("(") && formula.endsWith(""))
            formula = formula.substring(1, formula.length() - 1);

        ArrayList<CellReference> cellRefs = new ArrayList<CellReference>();
        for (AreaReference area : AreaReference.generateContiguous(formula)) {
            cellRefs.addAll(Arrays.asList(area.getAllReferencedCells()));
        }
        return cellRefs;
    }

    public static String getStringValue(CellReference ref,
            Spreadsheet spreadsheet) {
        return spreadsheet.getCellValue(spreadsheet.getCell(ref));
    }

    public static Double getNumericValue(CellReference ref,
            Spreadsheet spreadsheet) {
        try {
            spreadsheet.getFormulaEvaluator().evaluateFormulaCell(
                    spreadsheet.getCell(ref));

            if (spreadsheet.getCell(ref).getCellType() == Cell.CELL_TYPE_NUMERIC
                    || spreadsheet.getCell(ref).getCellType() == Cell.CELL_TYPE_FORMULA)
                return spreadsheet.getCell(ref).getNumericCellValue();
        } catch (NullPointerException e) {
        } catch (IllegalStateException e) {
        } catch (NumberFormatException e) {
        }
        return null;
    }
}
