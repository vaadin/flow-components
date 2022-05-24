package com.vaadin.flow.component.dialog.tests;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-dialog/header-footer")
public class DialogHeaderFooterIT extends AbstractComponentIT {

    private static final String OPEN_DIALOG_BUTTON = "open-dialog-button";
    private static final String CLOSE_DIALOG_BUTTON = "close-dialog-button";
    private static final String ATTACH_DIALOG_BUTTON = "attach-dialog-button";
    private static final String MOVE_BUTTONS_BUTTON = "move-buttons-button";
    private static final String ADD_HEADER_TITLE_BUTTON = "add-header-title-button";
    private static final String REMOVE_HEADER_TITLE_BUTTON = "remove-header-title-button";
    private static final String ADD_HEADER_CONTENT_BUTTON = "add-header-content-button";
    private static final String ADD_SECOND_HEADER_CONTENT_BUTTON = "add-second-header-content-button";
    private static final String REMOVE_HEADER_CONTENT_BUTTON = "remove-header-content-button";
    private static final String ADD_FOOTER_CONTENT_BUTTON = "add-footer-content-button";
    private static final String ADD_SECOND_FOOTER_CONTENT_BUTTON = "add-second-footer-content-button";
    private static final String REMOVE_FOOTER_CONTENT_BUTTON = "remove-footer-content-button";

    @Before
    public void init() {
        open();
    }

    @Test
    public void closedDialog_headerTitleIsSet_titleRendered() {
        clickButton(ADD_HEADER_TITLE_BUTTON);
        clickButton(OPEN_DIALOG_BUTTON);

        verifyContentRendered(DialogHeaderFooterPage.HEADER_TITLE);
    }

    @Test
    public void openedDialog_headerTitleIsSet_titleRendered() {
        clickButton(OPEN_DIALOG_BUTTON);
        verifyDialogOpened();
        clickButton(MOVE_BUTTONS_BUTTON);
        clickButton(ADD_HEADER_TITLE_BUTTON);

        verifyContentRendered(DialogHeaderFooterPage.HEADER_TITLE);
    }

    @Test
    public void openedDialogWithHeaderTitle_headerTitleIsUnset_noTitleRendered() {
        clickButton(ADD_HEADER_TITLE_BUTTON);
        clickButton(OPEN_DIALOG_BUTTON);
        verifyDialogOpened();
        clickButton(MOVE_BUTTONS_BUTTON);
        clickButton(REMOVE_HEADER_TITLE_BUTTON);

        assertDialogNotContains(DialogHeaderFooterPage.HEADER_TITLE);
    }

    @Test
    public void closedDialog_headerRendererIsSet_contentIsRendered() {
        clickButton(ADD_HEADER_CONTENT_BUTTON);
        clickButton(OPEN_DIALOG_BUTTON);

        verifyContentRendered(DialogHeaderFooterPage.HEADER_CONTENT);
    }

    @Test
    public void attachedAndClosedDialog_headerContentIsSet_contentIsRendered() {
        clickButton(ATTACH_DIALOG_BUTTON);
        clickButton(ADD_HEADER_CONTENT_BUTTON);
        clickButton(OPEN_DIALOG_BUTTON);

        verifyContentRendered(DialogHeaderFooterPage.HEADER_CONTENT);
    }

    @Test
    public void autoAttachedDialogWithHeaderContent_dialogReopened_contentIsRendered() {
        clickButton(ADD_HEADER_CONTENT_BUTTON);
        clickButton(OPEN_DIALOG_BUTTON);
        verifyDialogOpened();
        clickButton(CLOSE_DIALOG_BUTTON);
        clickButton(OPEN_DIALOG_BUTTON);

        verifyContentRendered(DialogHeaderFooterPage.HEADER_CONTENT);
    }

    @Test
    public void attachedDialogWithHeaderContent_dialogReopened_contentIsRendered() {
        clickButton(ATTACH_DIALOG_BUTTON);
        clickButton(ADD_HEADER_CONTENT_BUTTON);
        clickButton(OPEN_DIALOG_BUTTON);
        verifyDialogOpened();
        clickButton(CLOSE_DIALOG_BUTTON);
        clickButton(OPEN_DIALOG_BUTTON);

        verifyContentRendered(DialogHeaderFooterPage.HEADER_CONTENT);
    }

    @Test
    public void openedDialogWithHeaderContent_anotherContentIsAdded_allElementsAreRendered() {
        clickButton(ADD_HEADER_CONTENT_BUTTON);
        clickButton(OPEN_DIALOG_BUTTON);
        verifyDialogOpened();
        clickButton(MOVE_BUTTONS_BUTTON);
        clickButton(ADD_SECOND_HEADER_CONTENT_BUTTON);

        assertDialogContains(DialogHeaderFooterPage.HEADER_CONTENT);
        assertDialogContains(DialogHeaderFooterPage.ANOTHER_HEADER_CONTENT);
    }

    @Test
    public void openedDialogWithHeaderContent_removeContent_noContentIsRendered() {
        clickButton(ADD_HEADER_CONTENT_BUTTON);
        clickButton(OPEN_DIALOG_BUTTON);
        verifyDialogOpened();
        clickButton(MOVE_BUTTONS_BUTTON);
        clickButton(REMOVE_HEADER_CONTENT_BUTTON);

        assertDialogNotContains(DialogHeaderFooterPage.HEADER_CONTENT);
    }

    @Test
    public void closedDialog_footerRendererIsSet_contentIsRendered() {
        clickButton(ADD_FOOTER_CONTENT_BUTTON);
        clickButton(OPEN_DIALOG_BUTTON);

        verifyContentRendered(DialogHeaderFooterPage.FOOTER_CONTENT);
    }

    @Test
    public void attachedAndClosedDialog_footerContentIsSet_contentIsRendered() {
        clickButton(ATTACH_DIALOG_BUTTON);
        clickButton(ADD_FOOTER_CONTENT_BUTTON);
        clickButton(OPEN_DIALOG_BUTTON);

        verifyContentRendered(DialogHeaderFooterPage.FOOTER_CONTENT);
    }

    @Test
    public void autoAttachedDialogWithFooterContent_dialogReopened_contentIsRendered() {
        clickButton(ADD_FOOTER_CONTENT_BUTTON);
        clickButton(OPEN_DIALOG_BUTTON);
        verifyDialogOpened();
        clickButton(CLOSE_DIALOG_BUTTON);
        clickButton(OPEN_DIALOG_BUTTON);

        verifyContentRendered(DialogHeaderFooterPage.FOOTER_CONTENT);
    }

    @Test
    public void attachedDialogWithFooterContent_dialogReopened_contentIsRendered() {
        clickButton(ATTACH_DIALOG_BUTTON);
        clickButton(ADD_FOOTER_CONTENT_BUTTON);
        clickButton(OPEN_DIALOG_BUTTON);
        verifyDialogOpened();
        clickButton(CLOSE_DIALOG_BUTTON);
        clickButton(OPEN_DIALOG_BUTTON);

        verifyContentRendered(DialogHeaderFooterPage.FOOTER_CONTENT);
    }

    @Test
    public void openedDialogWithFooterContent_anotherContentIsAdded_allElementsAreRendered() {
        clickButton(ADD_FOOTER_CONTENT_BUTTON);
        clickButton(OPEN_DIALOG_BUTTON);
        verifyDialogOpened();
        clickButton(MOVE_BUTTONS_BUTTON);
        clickButton(ADD_SECOND_FOOTER_CONTENT_BUTTON);

        assertDialogContains(DialogHeaderFooterPage.FOOTER_CONTENT);
        assertDialogContains(DialogHeaderFooterPage.ANOTHER_FOOTER_CONTENT);
    }

    @Test
    public void openedDialogWithFooterContent_removeContent_noContentIsRendered() {
        clickButton(ADD_FOOTER_CONTENT_BUTTON);
        clickButton(OPEN_DIALOG_BUTTON);
        verifyDialogOpened();
        clickButton(MOVE_BUTTONS_BUTTON);
        clickButton(REMOVE_FOOTER_CONTENT_BUTTON);

        assertDialogNotContains(DialogHeaderFooterPage.FOOTER_CONTENT);
    }

    private void clickButton(String id) {
        findElement(By.id(id)).click();
    }

    private void assertDialogContains(String text) {
        var overlay = getOverlayElement();
        Assert.assertTrue("Dialog should contains text " + text,
                overlay.getText().contains(text));
    }

    private void assertDialogNotContains(String text) {
        var overlay = getOverlayElement();
        Assert.assertFalse("Dialog should not contain text " + text,
                overlay.getText().contains(text));
    }

    private void verifyDialogOpened() {
        waitForElementPresent(
                By.cssSelector("vaadin-dialog-overlay div button"));
    }

    private void verifyContentRendered(String content) {
        waitUntil(c -> {
            try {
                WebElement el = findElement(
                        By.tagName("vaadin-dialog-overlay"));
                if (el.isDisplayed() && el.getText().contains(content)) {
                    return true;
                }
                return false;
            } catch (StaleElementReferenceException
                    | NoSuchElementException e) {
                return false;
            }
        });
    }

    private WebElement getOverlayElement() {
        return findElement(By.tagName("vaadin-dialog-overlay"));
    }
}
