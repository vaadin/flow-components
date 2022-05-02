package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.util.NoSuchElementException;

public class CommentIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        getDriver().get(getBaseURL());

        createNewSpreadsheet();
    }

    @Test
    public void commentOverlay_sheetWithCommentsIsLoaded_overlayIsShownForVisibleComments() {
        loadFile("cell_comments.xlsx");

        assertCommentPresent("Always Visible Comment.");
        assertCommentOverlayIsShownOnHover("first cell comment");
    }

    @Test
    public void commentOverlay_commentsAreSetFromServerSide_overlayIsShownForVisibleComments() {
        loadTestFixture(TestFixtures.Comments);

        assertCommentPresent("Always Visible Comment.");
        assertCommentOverlayIsShownOnHover("first cell comment");
    }

    @Test
    public void commentOverlay_userHoversInvalidFormula_overlayIsShown() {
        createNewSpreadsheet();
        SheetCellElement a1 = getSpreadsheet().getCellAt("A1");

        a1.setValue("=a");

        assertCommentOverlayIsShownOnHover("Invalid formula");
    }

    @Test
    public void openFileWithComment_deleteComment_NoException()
            throws InterruptedException {
        loadFile("comment_sheet.xlsx");

        SheetCellElement cell = getSpreadsheet().getCellAt(3, 3);
        cell.contextClick();
        clickItem("Delete comment");
        assertNoErrorIndicatorDetected();
    }

    @Test
    public void openFileWithComment_showComment_NoException()
            throws InterruptedException {
        loadFile("comment_sheet.xlsx");

        SheetCellElement cell = getSpreadsheet().getCellAt(3, 3);
        cell.contextClick();
        clickItem("Show comment");
        assertNoErrorIndicatorDetected();
    }

    @Test
    public void contextClick_on_commentIndicator() throws InterruptedException {
        loadFile("comment_sheet.xlsx");

        SheetCellElement cell = getSpreadsheet().getCellAt(3, 3);
        Assert.assertTrue(cell.hasCommentIndicator());
        WebElement triangle = cell
                .findElement(By.className("cell-comment-triangle"));
        new Actions(getDriver()).contextClick(triangle).build().perform();
        Assert.assertFalse(hasOption("Insert comment"));
    }

    @Test
    public void removeRow_removeRowWithComment_commentIsRemoved() {
        loadFile("cell_comments.xlsx"); // A1 has a comment

        getSpreadsheet().getRowHeader(1).contextClick();
        getSpreadsheet().getContextMenu().getItem("Delete row").click();

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return !getSpreadsheet().getCellAt("A1").hasCommentIndicator();
            }
        });
    }

    private void assertCommentOverlayIsShownOnHover(String commentContains) {
        moveMouseOverCell("A2");
        assertCommentNotPresent(commentContains);

        moveMouseOverCell("A1");

        assertCommentPresent(commentContains);
    }

    public void moveMouseOverCell(String cellAddress) {
        SheetCellElement cell = getSpreadsheet().getCellAt(cellAddress);
        WebElement cornerElement = driver
                .findElement(By.cssSelector(".v-spreadsheet > .corner"));

        new Actions(driver).moveToElement(cornerElement)
                .moveToElement(cell.getWrappedElement()).build().perform();
    }

    private void assertCommentPresent(final String text) {
        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return webDriver.findElements(By.xpath(
                        "//div[(@class='comment-overlay-label' or @class='comment-overlay-invalidformula')"
                                + " and contains(text(), '" + text + "')]"))
                        .size() > 0;
            }
        });
    }

    private void assertCommentNotPresent(final String text) {
        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return webDriver.findElements(By.xpath(
                        "//div[(@class='comment-overlay-label' or @class='comment-overlay-invalidformula')"
                                + " and contains(text(), '" + text + "')]"))
                        .size() == 0;
            }
        });
    }

}
