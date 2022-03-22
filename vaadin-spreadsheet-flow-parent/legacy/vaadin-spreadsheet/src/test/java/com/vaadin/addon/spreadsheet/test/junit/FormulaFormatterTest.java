package com.vaadin.addon.spreadsheet.test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;

import com.vaadin.addon.spreadsheet.FormulaFormatter;

public class FormulaFormatterTest {

    @Test
    public void cellFormulaLocalizationInput_formulaWithFinnishLocale_formulaFormattedCorrectly() {
        Locale locale = new Locale("fi", "FI");
        FormulaFormatter manager = new FormulaFormatter();

        assertEquals("SUM(C4,E4)",
                manager.unFormatFormulaValue("SUM(C4;E4)", locale));
        assertEquals("1.1 + 2", manager.unFormatFormulaValue("1,1 + 2", locale));
        assertEquals("IF(B2=3.2,1,0)",
                manager.unFormatFormulaValue("IF(B2=3,2;1;0)", locale));
        assertEquals("1 + 2", manager.unFormatFormulaValue("1 + 2", locale));
        assertEquals("1000 + 2000",
                manager.unFormatFormulaValue("1000 + 2000", locale));
        assertEquals("HYPERLINK(\"http://www,vaadin,com\",  \"ups\")",
                manager.unFormatFormulaValue(
                        "HYPERLINK(\"http://www,vaadin,com\";  \"ups\")",
                        locale));
    }

    @Test
    public void cellFormulaLocalizationInput_formulaWithItalianLocale_formulaFormattedCorrectly() {
        Locale locale = new Locale("it", "IT");
        FormulaFormatter manager = new FormulaFormatter();

        assertEquals("SUM(C4,E4)",
                manager.unFormatFormulaValue("SUM(C4;E4)", locale));
        assertEquals("1.1 + 2", manager.unFormatFormulaValue("1,1 + 2", locale));
        assertEquals("IF(B2=3.2,1,0)",
                manager.unFormatFormulaValue("IF(B2=3,2;1;0)", locale));
        assertEquals("1 + 2", manager.unFormatFormulaValue("1 + 2", locale));
        assertEquals("1000.2 + 2000.1",
                manager.unFormatFormulaValue("1000,20 + 2000,10", locale));
        assertEquals("HYPERLINK(\"http://www,vaadin,com\",  \"ups\")",
                manager.unFormatFormulaValue(
                        "HYPERLINK(\"http://www,vaadin,com\";  \"ups\")",
                        locale));
    }

    @Test
    public void cellFormulaLocalizationInput_formulaWithUSLocale_formulaFormattedCorrectly() {
        Locale locale = new Locale("en", "US");
        FormulaFormatter manager = new FormulaFormatter();

        assertEquals("1000.20 + 2000.10",
                manager.unFormatFormulaValue("1000.20 + 2000.10", locale));
    }

    @Test
    public void cellFormulaLocalizationOutput_formulaWithFinnishLocale_formulaFormattedCorrectly() {
        Locale locale = new Locale("fi", "FI");
        FormulaFormatter manager = new FormulaFormatter();

        assertEquals("SUM(C4;E4)",
                manager.reFormatFormulaValue("SUM(C4,E4)", locale));
        assertEquals("1,1 + 2", manager.reFormatFormulaValue("1.1 + 2", locale));
        assertEquals("IF(B2=3,2;1;0)",
                manager.reFormatFormulaValue("IF(B2=3.2,1,0)", locale));
        assertEquals("1 + 2", manager.reFormatFormulaValue("1 + 2", locale));
        assertEquals("1000 + 2000",
                manager.reFormatFormulaValue("1000 + 2000", locale));
        assertEquals("HYPERLINK(\"http://www,vaadin,com\";  \"ups\")",
                manager.reFormatFormulaValue(
                        "HYPERLINK(\"http://www,vaadin,com\",  \"ups\")",
                        locale));
    }

    @Test
    public void cellFormulaLocalizationOutput_formulaWithItalianLocale_formulaFormattedCorrectly() {
        Locale locale = new Locale("it", "IT");
        FormulaFormatter manager = new FormulaFormatter();

        assertEquals("SUM(C4;E4)",
                manager.reFormatFormulaValue("SUM(C4,E4)", locale));
        assertEquals("1,1 + 2", manager.reFormatFormulaValue("1.1 + 2", locale));
        assertEquals("IF(B2=3,2;1;0)",
                manager.reFormatFormulaValue("IF(B2=3.2,1,0)", locale));
        assertEquals("1 + 2", manager.reFormatFormulaValue("1 + 2", locale));
        assertEquals("1000,2 + 2000,1",
                manager.reFormatFormulaValue("1000.20 + 2000.10", locale));
        assertEquals("HYPERLINK(\"http://www,vaadin,com\",  \"ups\")",
                manager.unFormatFormulaValue(
                        "HYPERLINK(\"http://www,vaadin,com\";  \"ups\")",
                        locale));
    }

    @Test
    public void cellFormulaLocalizationOutput_formulaWithUSLocale_formulaFormattedCorrectly() {
        Locale locale = new Locale("en", "US");
        FormulaFormatter manager = new FormulaFormatter();

        assertEquals("1000.20 + 2000.10",
                manager.reFormatFormulaValue("1000.20 + 2000.10", locale));
    }

    @Test
    public void cellFormulationValidation_validInputFormulasWithFinnishLocale_formulaValid() {
        final FormulaFormatter formulaFormatter = new FormulaFormatter();
        Locale locale = new Locale("fi", "FI");

        assertTrue(formulaFormatter.isValidFormulaFormat("=SUM(C4;E4)", locale));
        assertTrue(formulaFormatter.isValidFormulaFormat("=1,1 + 2", locale));
        assertTrue(formulaFormatter.isValidFormulaFormat("=IF(B2=3,2;1;0)",
                locale));
        assertTrue(formulaFormatter.isValidFormulaFormat("=1 + 2", locale));
        assertTrue(formulaFormatter.isValidFormulaFormat("=1000,20 + 2000,10",
                locale));
        assertTrue(formulaFormatter.isValidFormulaFormat(
                "=HYPERLINK(\"http://www.vaadin,com\";  \"ups\")", locale));
    }

    @Test
    public void cellFormulationValidation_validInputFormulasWithItalianLocale_formulaValid() {
        final FormulaFormatter formulaFormatter = new FormulaFormatter();
        Locale locale = new Locale("it", "IT");

        assertTrue(formulaFormatter.isValidFormulaFormat("=SUM(C4;E4)", locale));
        assertTrue(formulaFormatter.isValidFormulaFormat("=1,1 + 2", locale));
        assertTrue(formulaFormatter.isValidFormulaFormat("=IF(B2=3,2;1;0)",
                locale));
        assertTrue(formulaFormatter.isValidFormulaFormat("=1 + 2", locale));
        assertTrue(formulaFormatter.isValidFormulaFormat("=1000,20 + 2000,10",
                locale));
        assertTrue(formulaFormatter.isValidFormulaFormat(
                "=HYPERLINK(\"http://www.vaadin,com\";  \"ups\")", locale));
    }

    @Test
    public void cellFormulationValidation_inputWithInvalidDecimalSeparator_formulaNotValid() {
        final FormulaFormatter formulaFormatter = new FormulaFormatter();
        Locale locale = new Locale("it", "IT");

        assertFalse(formulaFormatter.isValidFormulaFormat("=1.1 + 1", locale));
    }
}
