package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.List;
import java.util.stream.Collectors;

public class CellDeletionIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        getDriver().get(getBaseURL());

        createNewSpreadsheet();
        loadTestFixture(TestFixtures.DeletionHandler);
    }

    @Test
    public void deletionHandler_SpreadsheetWithDeletionFixture_deleteSingleCellSucceedsWhenHandlerReturnsTrue() {
        clickCell("B2");

        new Actions(getDriver()).sendKeys(Keys.DELETE).build().perform();

        Assert.assertEquals("", getCellContent("B2"));

        assertNotificationContent("Deleting: 1:1");
    }

    @Test
    public void deletionHandler_SpreadsheetWithDeletionFixture_deleteSingleCellFailsWhenHandlerReturnsFalse() {
        clickCell("C2");

        new Actions(getDriver()).sendKeys(Keys.DELETE).build().perform();

        Assert.assertEquals("Try to delete me!", getCellContent("C2"));

        assertNotificationContent("Attempting to delete: 1:2");
    }

    @Test
    public void deletionHandler_SpreadsheetWithDeletionFixture_deleteIndividualCellSucceedsWhenHandlerReturnsTrue() {
        clickCell("B3");
        getSpreadsheet().getCellAt("B5").click(5, 5, Keys.CONTROL);

        new Actions(getDriver()).sendKeys(Keys.DELETE).build().perform();

        Assert.assertEquals("", getCellContent("B3"));
        Assert.assertEquals("", getCellContent("B5"));

        assertNotificationContent("Deleting: 2:1;4:1");
    }

    @Test
    public void deletionHandler_SpreadsheetWithDeletionFixture_deleteIndividualCellFailsWhenHandlerReturnsFalse() {
        clickCell("C3");
        getSpreadsheet().getCellAt("C5").click(5, 5, Keys.CONTROL);

        new Actions(getDriver()).sendKeys(Keys.DELETE).build().perform();

        Assert.assertEquals("Try to delete us too!", getCellContent("C3"));
        Assert.assertEquals("Try to delete us too!", getCellContent("C5"));

        assertNotificationContent("Attempting to delete: 2:2;4:2");
    }

    @Test
    public void deletionHandler_SpreadsheetWithDeletionFixture_deleteCellRangeSucceedsWhenHandlerReturnsTrue() {
        clickCell("B6");
        getSpreadsheet().getCellAt("B8").click(5, 5, Keys.SHIFT);

        new Actions(getDriver()).sendKeys(Keys.DELETE).build().perform();

        Assert.assertEquals("", getCellContent("B6"));
        Assert.assertEquals("", getCellContent("B7"));
        Assert.assertEquals("", getCellContent("B8"));

        assertNotificationContent("Deleting: 5:1-7:1");
    }

    @Test
    public void deletionHandler_SpreadsheetWithDeletionFixture_deleteCellRangeFailsWhenHandlerReturnsFalse() {
        clickCell("C6");
        getSpreadsheet().getCellAt("C8").click(5, 5, Keys.SHIFT);

        new Actions(getDriver()).sendKeys(Keys.DELETE).build().perform();

        Assert.assertEquals("Try to delete this range!", getCellContent("C6"));
        Assert.assertEquals("Try to delete this range!", getCellContent("C7"));
        Assert.assertEquals("Try to delete this range!", getCellContent("C8"));

        assertNotificationContent("Attempting to delete: 5:2-7:2");
    }

    private void assertNotificationContent(String expected) {
        List<String> notifications = getNotifications().stream()
                .map(WebElement::getText).collect(Collectors.toList());
        Assert.assertTrue(String.format(
                "Expected any of the notifications to contain the string '%s' but neither of them did. Notifications: '%s'",
                expected, notifications),
                notifications.stream().anyMatch(
                        notification -> notification.contains(expected)));
    }

    private List<WebElement> getNotifications() {
        return findElements(By.tagName("vaadin-notification-card"));
    }
}
