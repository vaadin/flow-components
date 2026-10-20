/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet")
public class CommentIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();

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
        // The triangle is a sibling of its cell in the grid, positioned in
        // grid coordinates, so it is found by its anchor rather than by
        // searching inside the cell element.
        WebElement triangle = findElementInShadowRoot(By.cssSelector(
                "[part~=\"comment-triangle\"][data-col=\"3\"][data-row=\"3\"]"));
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
        WebElement cornerElement = findElementInShadowRoot(
                By.cssSelector(".sheet-header-corner"));

        new Actions(driver).moveToElement(cornerElement)
                .moveToElement(cell.getWrappedElement()).build().perform();
    }

    private void assertCommentPresent(final String text) {
        waitUntil(webDriver -> commentOverlayCount(text) > 0);
    }

    private void assertCommentNotPresent(final String text) {
        waitUntil(webDriver -> commentOverlayCount(text) == 0);
    }

    /**
     * Comment notes carrying the given text.
     * <p>
     * They are drawn in the cell grid's shadow root, which no document query
     * reaches.
     */
    private long commentOverlayCount(String text) {
        Object count = executeScript("""
                const grid = arguments[0].shadowRoot.querySelector(
                    'vaadin-spreadsheet-internal-cell-grid');
                if (!grid || !grid.shadowRoot) return 0;
                const notes = grid.shadowRoot.querySelectorAll(
                    '.comment-overlay-label, .comment-overlay-invalidformula');
                let found = 0;
                for (const note of notes) {
                    if (note.textContent.includes(arguments[1])) found++;
                }
                return found;
                """, getSpreadsheet(), text);
        return count instanceof Long ? (Long) count : 0L;
    }

}
