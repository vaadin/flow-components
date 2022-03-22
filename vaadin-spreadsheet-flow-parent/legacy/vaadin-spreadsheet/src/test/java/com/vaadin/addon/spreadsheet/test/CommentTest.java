package com.vaadin.addon.spreadsheet.test;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.addon.spreadsheet.test.fixtures.TestFixtures;
import com.vaadin.addon.spreadsheet.test.testutil.ContextMenuHelper;
import com.vaadin.testbench.parallel.Browser;

public class CommentTest extends AbstractSpreadsheetTestCase {

    @Test
    public void commentOverlay_sheetWithCommentsIsLoaded_overlayIsShownForVisibleComments() {
        headerPage.createNewSpreadsheet();

        headerPage.loadFile("cell_comments.xlsx", this);

        assertCommentPresent("Always Visible Comment.");
        assertCommentOverlayIsShownOnHover("first cell comment");
    }

    @Test
    public void commentOverlay_commentsAreSetFromServerSide_overlayIsShownForVisibleComments() {
        headerPage.createNewSpreadsheet();

        headerPage.loadTestFixture(TestFixtures.Comments);

        assertCommentPresent("Always Visible Comment.");
        assertCommentOverlayIsShownOnHover("first cell comment");
    }

    @Test
    public void commentOverlay_userHoversInvalidFormula_overlayIsShown() {
        headerPage.createNewSpreadsheet();
        final SheetCellElement a1 = $(SpreadsheetElement.class).first()
                .getCellAt("A1");

        a1.setValue("=a");

        assertCommentOverlayIsShownOnHover("Invalid formula");
    }

    @Test
    public void openFileWithComment_deleteComment_NoException()
            throws InterruptedException {
        headerPage.loadFile("comment_sheet.xlsx", this);

        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        ContextMenuHelper contextMenu = new ContextMenuHelper(driver);
        SheetCellElement cell = spreadsheet.getCellAt(3, 3);
        cell.contextClick();
        contextMenu.clickItem("Delete comment");
        assertNoErrorIndicatorDetected();
    }

    @Test
    public void openFileWithComment_showComment_NoException()
            throws InterruptedException {
        headerPage.loadFile("comment_sheet.xlsx", this);

        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        ContextMenuHelper contextMenu = new ContextMenuHelper(driver);
        SheetCellElement cell = spreadsheet.getCellAt(3, 3);
        cell.contextClick();
        contextMenu.clickItem("Show comment");
        assertNoErrorIndicatorDetected();
    }

    @Test
    public void contextClick_on_commentIndicator()
            throws InterruptedException {
        headerPage.loadFile("comment_sheet.xlsx", this);

        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        ContextMenuHelper contextMenu = new ContextMenuHelper(driver);
        SheetCellElement cell = spreadsheet.getCellAt(3, 3);
        Assert.assertTrue(cell.hasCommentIndicator());
        WebElement triangle = cell.findElement(By.className("cell-comment-triangle"));
        new Actions(getDriver()).contextClick(triangle).build().perform();
        Assert.assertFalse(contextMenu.hasOption("Insert comment"));
    }


    @Test
    public void removeRow_removeRowWithComment_commentIsRemoved() {
        skipBrowser("Context click does not work with PhantomJS and Firefox",
                Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        headerPage.loadFile("cell_comments.xlsx", this); // A1 has a comment
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class)
                .first();

        spreadsheet.getRowHeader(1).contextClick();
        spreadsheet.getContextMenu().getItem("Delete row").click();

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return !spreadsheet.getCellAt("A1").hasCommentIndicator();
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
        SheetCellElement cell = $(SpreadsheetElement.class).first()
                .getCellAt(cellAddress);
        WebElement cornerElement = driver
                .findElement(By.cssSelector(".v-spreadsheet > .corner"));

        new Actions(driver).moveToElement(cornerElement)
                .moveToElement(cell.getWrappedElement()).build().perform();
    }

    private void assertCommentPresent(final String text) {
        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return webDriver.findElements(By
                        .xpath("//div[(@class='comment-overlay-label' or @class='comment-overlay-invalidformula')"
                                + " and contains(text(), '" + text + "')]"))
                        .size() > 0;
            }
        });
    }

    private void assertCommentNotPresent(final String text) {
        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return webDriver.findElements(By
                        .xpath("//div[(@class='comment-overlay-label' or @class='comment-overlay-invalidformula')"
                                + " and contains(text(), '" + text + "')]"))
                        .size() == 0;
            }
        });
    }

}
