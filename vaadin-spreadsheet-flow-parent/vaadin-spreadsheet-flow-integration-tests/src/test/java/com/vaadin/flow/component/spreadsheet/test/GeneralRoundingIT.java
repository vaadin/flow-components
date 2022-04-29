package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class GeneralRoundingIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    @Test
    public void generalFormat_spreadsheetWithGeneralFormatAndLocaleUS_numbersRoundedCorrectly() {
        setLocale(Locale.US);

        loadFile("general_round.xlsx");

        String cellBeforeResize = getSpreadsheet().getCellAt("A7").getValue();
        Assert.assertFalse(cellBeforeResize.contains(","));
        Assert.assertFalse(cellBeforeResize.contains("−"));
        Assert.assertFalse(cellBeforeResize.contains("#"));

        loadTestFixture(TestFixtures.FirstColumnWidth);

        String cellAfterResize = getSpreadsheet().getCellAt("A7").getValue();

        Assert.assertTrue("Number not shortened",
                cellAfterResize.length() < cellBeforeResize.length());
        Assert.assertFalse(cellAfterResize.contains(","));
        Assert.assertFalse(cellAfterResize.contains("−"));
        Assert.assertFalse(cellAfterResize.contains("#"));
    }

    @Test
    public void generalFormat_spreadsheetWithGeneralFormatAndLocaleFI_numbersRoundedCorrectly() {
        setLocale(new Locale("fi", "FI"));
        loadFile("general_round.xlsx");

        String cellBeforeResize = getSpreadsheet().getCellAt("A7").getValue();
        Assert.assertFalse(cellBeforeResize.contains("."));
        Assert.assertFalse(cellBeforeResize.contains("−"));
        Assert.assertFalse(cellBeforeResize.contains("#"));

        loadTestFixture(TestFixtures.FirstColumnWidth);

        String cellAfterResize = getSpreadsheet().getCellAt("A7").getValue();

        Assert.assertTrue("Number not shortened",
                cellAfterResize.length() < cellBeforeResize.length());
        Assert.assertFalse(cellAfterResize.contains(","));
        Assert.assertFalse(cellAfterResize.contains("−"));
        Assert.assertFalse(cellAfterResize.contains("#"));
    }

    @Test
    public void generalFormat_spreadsheetWithGeneralFormatAndLocaleUS_negativeNumbersRoundedCorrectly() {
        assertEquals("Check US locale", Locale.US.toString(), "en_US");
        setLocale(Locale.US);

        loadFile("negative_general_round.xlsx");

        String cellBeforeResize = getSpreadsheet().getCellAt("A7").getValue();
        Assert.assertFalse(cellBeforeResize.contains(","));
        Assert.assertTrue(cellBeforeResize.contains("-"));
        Assert.assertFalse(cellBeforeResize.contains("#"));

        loadTestFixture(TestFixtures.FirstColumnWidth);

        String cellAfterResize = getSpreadsheet().getCellAt("A7").getValue();

        Assert.assertTrue("Number not shortened",
                cellAfterResize.length() < cellBeforeResize.length());
        Assert.assertFalse(cellAfterResize.contains(","));
        Assert.assertTrue(cellAfterResize.contains("-"));
        Assert.assertFalse(cellAfterResize.contains("#"));
    }

    @Test
    @Ignore("Rounding is not applied with Finnish Locale")
    public void generalFormat_spreadsheetWithGeneralFormatAndLocaleFI_negativeNumbersRoundedCorrectly() {
        setLocale(new Locale("fi", "FI"));
        loadFile("negative_general_round.xlsx");

        String cellBeforeResize = getSpreadsheet().getCellAt("A7").getValue();
        Assert.assertFalse(cellBeforeResize.contains("."));
        Assert.assertTrue(cellBeforeResize.contains("−"));
        Assert.assertFalse(cellBeforeResize.contains("#"));

        loadTestFixture(TestFixtures.FirstColumnWidth);

        String cellAfterResize = getSpreadsheet().getCellAt("A7").getValue();

        // TODO: fix shortening the number in Finnish locale
        Assert.assertTrue("Number not shortened",
                cellAfterResize.length() < cellBeforeResize.length());
        Assert.assertFalse(cellAfterResize.contains("."));
        Assert.assertTrue(cellAfterResize.contains("−"));
        Assert.assertFalse(cellAfterResize.contains("#"));
    }

}
