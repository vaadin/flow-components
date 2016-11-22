package com.vaadin.addon.spreadsheet.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.addon.spreadsheet.test.fixtures.TestFixtures;
import com.vaadin.testbench.By;
import com.vaadin.testbench.parallel.Browser;

public class CellDeletionTest extends AbstractSpreadsheetTestCase {


    @Before
    public void setUp() throws Exception {
        super.setUp();
        headerPage.createNewSpreadsheet();
        headerPage.loadTestFixture(TestFixtures.DeletionHandler);
    }

    @Test
    public void deletionHandler_SpreadsheetWithDeletionFixture_deleteSingleCellSucceedsWhenHandlerReturnsTrue() {
        sheetController.clickCell("B2");

        new Actions(getDriver()).sendKeys(Keys.DELETE).build().perform();

        assertThat(sheetController.getCellContent("B2"), is(""));

        WebElement notification = findElement(By
                .className("v-Notification-caption"));
        assertThat(notification.getText(), is("Deleting: 1:1"));
    }

    @Test
    public void deletionHandler_SpreadsheetWithDeletionFixture_deleteSingleCellFailsWhenHandlerReturnsFalse() {
        sheetController.clickCell("C2");

        new Actions(getDriver()).sendKeys(Keys.DELETE).build().perform();

        assertThat(sheetController.getCellContent("C2"),
                is("Try to delete me!"));

        WebElement notification = findElement(By
                .className("v-Notification-caption"));
        assertThat(notification.getText(), is("Attempting to delete: 1:2"));
    }

    @Test
    public void deletionHandler_SpreadsheetWithDeletionFixture_deleteIndividualCellSucceedsWhenHandlerReturnsTrue() {
        skipBrowser("PhantomJS ignores the CTRL", Browser.PHANTOMJS);
        skipBrowser("Firefox ignores the CTRL", Browser.FIREFOX);

        sheetController.clickCell("B3");
        testBenchElement(sheetController.getCellElement("B5")).click(5, 5,
                Keys.CONTROL);

        new Actions(getDriver()).sendKeys(Keys.DELETE).build().perform();

        assertThat(sheetController.getCellContent("B3"), is(""));
        assertThat(sheetController.getCellContent("B5"), is(""));

        WebElement notification = findElement(By
                .className("v-Notification-caption"));
        assertThat(notification.getText(), is("Deleting: 2:1;4:1"));
    }

    @Test
    public void deletionHandler_SpreadsheetWithDeletionFixture_deleteIndividualCellFailsWhenHandlerReturnsFalse() {
        skipBrowser("PhantomJS ignores the CTRL", Browser.PHANTOMJS);
        skipBrowser("Firefox ignores the CTRL", Browser.FIREFOX);

        sheetController.clickCell("C3");
        testBenchElement(sheetController.getCellElement("C5")).click(5, 5,
                Keys.CONTROL);

        new Actions(getDriver()).sendKeys(Keys.DELETE).build().perform();

        assertThat(sheetController.getCellContent("C3"),
                is("Try to delete us too!"));
        assertThat(sheetController.getCellContent("C5"),
                is("Try to delete us too!"));

        WebElement notification = findElement(By
                .className("v-Notification-caption"));
        assertThat(notification.getText(), is("Attempting to delete: 2:2;4:2"));
    }

    @Test
    public void deletionHandler_SpreadsheetWithDeletionFixture_deleteCellRangeSucceedsWhenHandlerReturnsTrue() {
        skipBrowser("PhantomJS ignores the SHIFT", Browser.PHANTOMJS);
        skipBrowser("Firefox ignores the SHIFT", Browser.FIREFOX);

        sheetController.clickCell("B6");
        testBenchElement(sheetController.getCellElement("B8")).click(5, 5,
                Keys.SHIFT);

        new Actions(getDriver()).sendKeys(Keys.DELETE).build().perform();

        assertThat(sheetController.getCellContent("B6"), is(""));
        assertThat(sheetController.getCellContent("B7"), is(""));
        assertThat(sheetController.getCellContent("B8"), is(""));

        WebElement notification = findElement(By
                .className("v-Notification-caption"));
        assertThat(notification.getText(), is("Deleting: 5:1-7:1"));
    }

    @Test
    public void deletionHandler_SpreadsheetWithDeletionFixture_deleteCellRangeFailsWhenHandlerReturnsFalse() {
        skipBrowser("PhantomJS ignores the SHIFT", Browser.PHANTOMJS);
        skipBrowser("Firefox ignores the SHIFT", Browser.FIREFOX);

        sheetController.clickCell("C6");
        testBenchElement(sheetController.getCellElement("C8")).click(5, 5,
                Keys.SHIFT);

        new Actions(getDriver()).sendKeys(Keys.DELETE).build().perform();

        assertThat(sheetController.getCellContent("C6"),
                is("Try to delete this range!"));
        assertThat(sheetController.getCellContent("C7"),
                is("Try to delete this range!"));
        assertThat(sheetController.getCellContent("C8"),
                is("Try to delete this range!"));

        WebElement notification = findElement(By
                .className("v-Notification-caption"));
        assertThat(notification.getText(), is("Attempting to delete: 5:2-7:2"));
    }
}
