package com.vaadin.addon.spreadsheet.test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import java.util.Locale;

import org.junit.Test;

import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.addon.spreadsheet.test.fixtures.TestFixtures;

public class GeneralSmallRoundingTest extends AbstractSpreadsheetTestCase {

    public static final String TARGET_CELL = "A6";

    @Test
    public void generalFormat_spreadsheetWithGeneralFormatAndLocaleFI_smallNmbersRoundedCorrectly() {
        //TODO Vaadin8 use setLocale instead of setLocaleForNativeSElect
        //When https://github.com/vaadin/framework8-issues/issues/477 is fixed
        setLocale(new Locale("fi", "FI"));
        headerPage.loadFile("general_round.xlsx", this);
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();

        String cellBeforeResize = spreadsheet.getCellAt(TARGET_CELL).getValue();
        assertThat(cellBeforeResize, not(containsString(".")));
        assertThat(cellBeforeResize, not(containsString("-")));
        assertThat(cellBeforeResize, not(containsString("#")));

        headerPage.loadTestFixture(TestFixtures.FirstColumnWidth);

        String cellAfterResize = spreadsheet.getCellAt(TARGET_CELL).getValue();

        assertThat("Number not shortened", cellAfterResize.length(),
                lessThan(cellBeforeResize.length()));
        assertThat(cellAfterResize, not(containsString(".")));
        assertThat(cellAfterResize, not(containsString("-")));
        assertThat(cellAfterResize, not(containsString("#")));
    }

    @Test
    public void generalFormat_spreadsheetWithGeneralFormatAndLocaleUS_negativeNumbersRoundedCorrectly() {
        //TODO Vaadin8 use setLocale instead of setLocaleForNativeSElect
        //setLocale(Locale.US);
        //When https://github.com/vaadin/framework8-issues/issues/477 is fixed
        setLocale(Locale.US);
        headerPage.loadFile("negative_general_round.xlsx", this);
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();

        String cellBeforeResize = spreadsheet.getCellAt(TARGET_CELL).getValue();
        assertThat(cellBeforeResize, not(containsString(",")));
        assertThat(cellBeforeResize, containsString("-"));
        assertThat(cellBeforeResize, not(containsString("#")));

        headerPage.loadTestFixture(TestFixtures.FirstColumnWidth);

        String cellAfterResize = spreadsheet.getCellAt(TARGET_CELL).getValue();

        assertThat("Number not shortened", cellAfterResize.length(),
                lessThan(cellBeforeResize.length()));
        assertThat(cellAfterResize, not(containsString(",")));
        assertThat(cellAfterResize, containsString("-"));
        assertThat(cellAfterResize, not(containsString("#")));
    }

    @Test
    public void generalFormat_spreadsheetWithGeneralFormatAndLocaleFI_negativeNumbersRoundedCorrectly() {
        //TODO Vaadin8 use setLocale instead of setLocaleForNativeSElect
        //When https://github.com/vaadin/framework8-issues/issues/477 is fixed
        setLocale(new Locale("fi", "FI"));
        headerPage.loadFile("negative_general_round.xlsx", this);
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();

        String cellBeforeResize = spreadsheet.getCellAt(TARGET_CELL).getValue();
        assertThat(cellBeforeResize, not(containsString(".")));
        assertThat(cellBeforeResize, containsString("-"));
        assertThat(cellBeforeResize, not(containsString("#")));

        headerPage.loadTestFixture(TestFixtures.FirstColumnWidth);

        String cellAfterResize = spreadsheet.getCellAt(TARGET_CELL).getValue();

        assertThat("Number not shortened", cellAfterResize.length(),
                lessThan(cellBeforeResize.length()));
        assertThat(cellAfterResize, not(containsString(".")));
        assertThat(cellAfterResize, containsString("-"));
        assertThat(cellAfterResize, not(containsString("#")));
    }
}
