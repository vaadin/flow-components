package com.vaadin.flow.component.spreadsheet.test;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ConditionalFormattingBasedOnSharedFormulaIT
        extends AbstractSpreadsheetIT {

    private static final String FALSE_CONDITION_COLOR = "rgba(255, 255, 255, 1)";
    private static final String TRUE_CONDITION_COLOR = "rgba(255, 0, 0, 1)";

    private static final Set<String> cellWithTrueCondition = Sets
            .newHashSet("A2", "A3", "A4", "D1", "D3");
    private static final Set<String> cellWithFormattingCondition = getCells();

    @Before
    public void init() {
        getDriver().get(getBaseURL());

        loadFile("conditional_formatting_shared_formula.xlsx");
    }

    @Test
    public void loadSpreadsheetWithConditionalFormattingInA1A2_A3B4_D1G5___CheckCellFormatting() {
        for (String cellAddress : cellWithTrueCondition) {
            assertEquals(TRUE_CONDITION_COLOR, getCellColor(cellAddress));
        }
        for (String cellAddress : cellWithFormattingCondition) {
            if (!cellWithTrueCondition.contains(cellAddress)) {
                assertEquals(FALSE_CONDITION_COLOR, getCellColor(cellAddress));
            }
        }
    }

    private static Set<String> getCells() {
        Set<String> firstCellRange = Sets.newHashSet("A1", "A2");
        Set<String> secondCellRange = Sets.newHashSet("A3", "A4", "B3", "B4");
        Set<String> thirdCellRange = new HashSet<String>();
        for (int i = 1; i <= 5; i++) {
            for (String column : new String[] { "D", "E", "F", "G" }) {
                thirdCellRange.add(column + i);
            }
        }

        Set<String> union = new HashSet<String>(firstCellRange);
        union.addAll(secondCellRange);
        union.addAll(thirdCellRange);
        return union;
    }
}
