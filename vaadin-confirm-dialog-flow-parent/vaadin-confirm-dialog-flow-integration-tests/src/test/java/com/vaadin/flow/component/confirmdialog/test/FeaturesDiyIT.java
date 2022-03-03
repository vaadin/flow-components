package com.vaadin.flow.component.confirmdialog.test;

import com.vaadin.flow.component.confirmdialog.examples.FeaturesDiy;
import com.vaadin.flow.component.confirmdialog.test.helpers.Actionwords;
import com.vaadin.tests.AbstractParallelTest;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

public class FeaturesDiyIT extends AbstractParallelTest {

    public Actionwords actionwords = new Actionwords(this, FeaturesDiy.class);

    @Test
    public void testReminderDialog() throws Exception {
        String sampleName = "SampleConfirmDialog";
        actionwords.iHaveSampleDialog(sampleName);
        actionwords.iOpenDialogDialog(sampleName);
        actionwords.iCompareTheDialogToReferenceImage(sampleName);
        actionwords.iClickConfirm();
        actionwords.dialogDialogIsClosed(sampleName);
        actionwords.confirmEventIsFired();
    }

    @Test
    public void testConfirmDeleteDialog() throws Exception {
        String sampleName = "SampleConfirmDeleteDialog";
        actionwords.iHaveSampleDialog(sampleName);
        actionwords.iOpenDialogDialog(sampleName);
        actionwords.iCompareTheDialogToReferenceImage(sampleName);
        actionwords.iClickConfirm();
        actionwords.dialogDialogIsClosed(sampleName);
        actionwords.confirmEventIsFired();
        actionwords.iOpenDialogDialog(sampleName);
        actionwords.iClickCancel();
        actionwords.dialogDialogIsClosed(sampleName);
        actionwords.cancelEventIsFired();
    }

    @Test
    public void testAreYouSureYouWantToPublishDialog() throws Exception {
        String sampleName = "SampleConfirmPublishDialog";
        actionwords.iHaveSampleDialog(sampleName);
        actionwords.iOpenDialogDialog(sampleName);
        actionwords.iCompareTheDialogToReferenceImage(sampleName);
        actionwords.iClickConfirm();
        actionwords.dialogDialogIsClosed(sampleName);
        actionwords.confirmEventIsFired();
        actionwords.iOpenDialogDialog(sampleName);
        actionwords.iClickCancel();
        actionwords.dialogDialogIsClosed(sampleName);
        actionwords.cancelEventIsFired();
    }

    @Test
    public void testUnsavedChangesDialog() throws Exception {
        String sampleName = "SampleUnsavedChangesDialog";
        actionwords.iHaveSampleDialog(sampleName);
        actionwords.iOpenDialogDialog(sampleName);
        actionwords.iCompareTheDialogToReferenceImage(sampleName);
        actionwords.iClickConfirm();
        actionwords.dialogDialogIsClosed(sampleName);
        actionwords.confirmEventIsFired();
        actionwords.iOpenDialogDialog(sampleName);
        actionwords.iClickReject();
        actionwords.dialogDialogIsClosed(sampleName);
        actionwords.rejectEventIsFired();
        actionwords.iOpenDialogDialog(sampleName);
        actionwords.iClickCancel();
        actionwords.dialogDialogIsClosed(sampleName);
        actionwords.cancelEventIsFired();
    }

    @Test
    public void testConfirmDialogButtonsCount() throws Exception {
        String sampleName = "SampleConfirmDialog";
        actionwords.iHaveSampleDialog(sampleName);
        actionwords.iOpenDialogDialog(sampleName);
        int confirmButtonCount = findElements(By.cssSelector(
                "vaadin-confirm-dialog-overlay [slot='confirm-button']"))
                        .size();
        Assert.assertEquals(1, confirmButtonCount);
    }
}
