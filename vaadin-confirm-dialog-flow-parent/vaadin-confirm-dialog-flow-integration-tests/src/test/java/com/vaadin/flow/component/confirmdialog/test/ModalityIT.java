package com.vaadin.flow.component.confirmdialog.test;

import com.vaadin.flow.component.html.testbench.SpanElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.openqa.selenium.By;

@TestPath("vaadin-confirm-dialog/modality")
public class ModalityIT extends AbstractComponentIT {

    private ButtonElement openAutoAddedDialog;
    private ButtonElement openManuallyAddedDialog;
    private ButtonElement testClick;
    private SpanElement testClickResult;

    @Before
    public void init() {
        open();
        openAutoAddedDialog = $(ButtonElement.class)
                .id("open-auto-added-dialog");
        openManuallyAddedDialog = $(ButtonElement.class)
                .id("open-manually-added-dialog");
        testClick = $(ButtonElement.class).id("test-click");
        testClickResult = $(SpanElement.class).id("test-click-result");
    }

    @Test
    public void openAutoAddedDialog_noClicksOnElementsInBackgroundAllowed() {
        openAutoAddedDialog.click();
        // Wait for dialog to exist in DOM
        getDialog("auto-added-dialog");
        assertBackgroundButtonIsNotClickable();
    }

    @Test
    public void openAndCloseAutoAddedDialog_clicksOnElementsInBackgroundAllowed() {
        openAutoAddedDialog.click();
        ConfirmDialogElement dialogElement = getDialog("auto-added-dialog");
        dialogElement.getConfirmButton().click();
        assertBackgroundButtonIsClickable();
    }

    @Test
    public void openManuallyAddedDialog_noClicksOnElementsInBackgroundAllowed() {
        openManuallyAddedDialog.click();
        // Wait for dialog to exist in DOM
        getDialog("manually-added-dialog");
        assertBackgroundButtonIsNotClickable();
    }

    @Test
    public void openAndCloseManuallyAddedDialog_clicksOnElementsInBackgroundAllowed() {
        openManuallyAddedDialog.click();
        ConfirmDialogElement dialogElement = getDialog("manually-added-dialog");
        dialogElement.getConfirmButton().click();
        assertBackgroundButtonIsClickable();
    }

    private ConfirmDialogElement getDialog(String id) {
        waitForElementPresent(By.id(id));
        return $(ConfirmDialogElement.class).id(id);
    }

    private void assertBackgroundButtonIsNotClickable() {
        testClick.click();
        Assert.assertEquals("Button in background is clickable", "",
                testClickResult.getText());
    }

    private void assertBackgroundButtonIsClickable() {
        testClick.click();
        Assert.assertEquals("Button in background is not clickable",
                "Click event received", testClickResult.getText());
    }
}
