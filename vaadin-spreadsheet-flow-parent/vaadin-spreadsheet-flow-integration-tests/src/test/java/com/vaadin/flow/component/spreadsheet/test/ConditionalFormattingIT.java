package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet")
public class ConditionalFormattingIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();

        loadFile("conditional_formatting_with_Invalid_formula.xlsx");
    }

    @Test
    public void unsupportedFormula_parse_noAffectCondtionalFormat() {
        String value = "rgba(255, 235, 156, 1)";
        Assert.assertEquals(value, getSpreadsheet().getCellAt("B1")
                .getCssValue("background-color"));
    }

    @Test
    public void unsupportedFormula_parse_noErrorIndicator() {
        assertNoErrorIndicatorDetected();
    }

}
