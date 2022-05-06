package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConditionalFormattingIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        getDriver().get(getBaseURL());

        loadFile("conditional_formatting_with_Invalid_formula.xlsx");
    }

    @Test
    public void unsupportedFormula_parse_noAffectCondtionalFormat() {
        String value = "rgba(255, 235, 156, 1)";
        assertEquals(value, getSpreadsheet().getCellAt("B1")
                .getCssValue("background-color"));
    }

    @Test
    public void unsupportedFormula_parse_noErrorIndicator() {
        assertNoErrorIndicatorDetected();
    }

}
