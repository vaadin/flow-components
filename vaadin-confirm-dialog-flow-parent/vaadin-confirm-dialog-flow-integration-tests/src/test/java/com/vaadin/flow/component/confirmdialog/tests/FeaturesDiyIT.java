/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.confirmdialog.tests;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-confirm-dialog/FeaturesDiy")
public class FeaturesDiyIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void testReminderDialog() throws Exception {
        openDialog("SampleConfirmDialog");
        clickConfirm();
        assertDialogClosed();
        assertConfirmEventIsFired();
    }

    @Test
    public void testConfirmDeleteDialog() throws Exception {
        String sampleName = "SampleConfirmDeleteDialog";
        openDialog(sampleName);
        clickConfirm();
        assertDialogClosed();
        assertConfirmEventIsFired();

        openDialog(sampleName);
        clickCancel();
        assertDialogClosed();
        assertCancelEventIsFired();
    }

    @Test
    public void testAreYouSureYouWantToPublishDialog() throws Exception {
        String sampleName = "SampleConfirmPublishDialog";
        openDialog(sampleName);
        clickConfirm();
        assertDialogClosed();
        assertConfirmEventIsFired();

        openDialog(sampleName);
        clickCancel();
        assertDialogClosed();
        assertCancelEventIsFired();
    }

    @Test
    public void testUnsavedChangesDialog() throws Exception {
        String sampleName = "SampleUnsavedChangesDialog";
        openDialog(sampleName);
        clickConfirm();
        assertDialogClosed();
        assertConfirmEventIsFired();

        openDialog(sampleName);
        clickReject();
        assertDialogClosed();
        assertRejectEventIsFired();

        openDialog(sampleName);
        clickCancel();
        assertDialogClosed();
        assertCancelEventIsFired();
    }

    @Test
    public void testConfirmDialogButtonsCount() throws Exception {
        openDialog("SampleConfirmDialog");
        int confirmButtonCount = findElements(By.cssSelector(
                "vaadin-confirm-dialog-overlay [slot='confirm-button']"))
                .size();
        Assert.assertEquals(1, confirmButtonCount);
    }

    private void openDialog(String dialog) {
        $(ButtonElement.class).id(dialog).click();
    }

    private Optional<ConfirmDialogElement> getConfirmDialog() {
        ElementQuery<ConfirmDialogElement> query = $(ConfirmDialogElement.class)
                .onPage();
        return query.exists() ? Optional.of(query.first()) : Optional.empty();
    }

    public void assertDialogClosed() {
        Assert.assertFalse(getConfirmDialog().isPresent());
    }

    private void clickConfirm() {
        getConfirmDialog().get().getConfirmButton().click();
    }

    private void clickCancel() {
        getConfirmDialog().get().getCancelButton().click();
    }

    private void clickReject() {
        getConfirmDialog().get().getRejectButton().click();
    }

    private String getEventName() {
        return $(TestBenchElement.class).id("eventName").getText();
    }

    private void assertConfirmEventIsFired() {
        checkFiredEventName(ConfirmDialog.ConfirmEvent.class);
    }

    private void assertCancelEventIsFired() {
        checkFiredEventName(ConfirmDialog.CancelEvent.class);
    }

    private void assertRejectEventIsFired() {
        checkFiredEventName(ConfirmDialog.RejectEvent.class);
    }

    private void checkFiredEventName(Class<?> eventClass) {
        Assert.assertEquals(eventClass.getSimpleName(), getEventName());
    }
}
