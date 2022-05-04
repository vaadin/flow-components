package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Locale;

public class GeneralSmallRoundingIT extends AbstractSpreadsheetIT {

    public static final String TARGET_CELL = "A6";

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    @Test
    public void generalFormat_spreadsheetWithGeneralFormatAndLocaleFI_smallNmbersRoundedCorrectly() {
        setLocale(new Locale("fi", "FI"));
        loadFile("general_round.xlsx");

        String cellBeforeResize = getSpreadsheet().getCellAt(TARGET_CELL)
                .getValue();
        Assert.assertFalse(cellBeforeResize.contains("."));
        Assert.assertFalse(cellBeforeResize.contains("-"));
        Assert.assertFalse(cellBeforeResize.contains("#"));

        loadTestFixture(TestFixtures.FirstColumnWidth);

        String cellAfterResize = getSpreadsheet().getCellAt(TARGET_CELL)
                .getValue();

        Assert.assertTrue("Number not shortened",
                cellAfterResize.length() < cellBeforeResize.length());
        Assert.assertFalse(cellAfterResize.contains("."));
        Assert.assertFalse(cellAfterResize.contains("-"));
        Assert.assertFalse(cellAfterResize.contains("#"));
    }

    @Test
    public void generalFormat_spreadsheetWithGeneralFormatAndLocaleUS_negativeNumbersRoundedCorrectly() {
        setLocale(Locale.US);
        loadFile("negative_general_round.xlsx");

        String cellBeforeResize = getSpreadsheet().getCellAt(TARGET_CELL)
                .getValue();
        Assert.assertFalse(cellBeforeResize.contains(","));
        Assert.assertTrue(cellBeforeResize.contains("-"));
        Assert.assertFalse(cellBeforeResize.contains("#"));

        loadTestFixture(TestFixtures.FirstColumnWidth);

        String cellAfterResize = getSpreadsheet().getCellAt(TARGET_CELL)
                .getValue();

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

        String cellBeforeResize = getSpreadsheet().getCellAt(TARGET_CELL)
                .getValue();
        Assert.assertFalse(cellBeforeResize.contains("."));
        Assert.assertTrue(cellBeforeResize.contains("−"));
        Assert.assertFalse(cellBeforeResize.contains("#"));

        loadTestFixture(TestFixtures.FirstColumnWidth);

        String cellAfterResize = getSpreadsheet().getCellAt(TARGET_CELL)
                .getValue();

        // TODO: fix shortening the number in Finnish locale
        Assert.assertTrue("Number not shortened",
                cellAfterResize.length() < cellBeforeResize.length());
        Assert.assertFalse(cellAfterResize.contains("."));
        Assert.assertTrue(cellAfterResize.contains("−"));
        Assert.assertFalse(cellAfterResize.contains("#"));
    }
}
