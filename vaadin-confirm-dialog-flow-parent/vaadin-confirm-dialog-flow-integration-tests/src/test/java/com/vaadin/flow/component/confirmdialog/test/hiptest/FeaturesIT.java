package com.vaadin.flow.component.confirmdialog.test.hiptest;

import org.junit.Ignore;
import org.junit.Test;

import com.vaadin.flow.component.confirmdialog.test.AbstractParallelTest;

public class FeaturesIT extends AbstractParallelTest {

    public Actionwords actionwords = new Actionwords(this);

    public void reminderDialog(String sampleName) {
        actionwords.iHaveSampleDialog(sampleName);
        actionwords.iOpenDialogDialog(sampleName);
        actionwords.iCompareTheDialogToReferenceImage(sampleName);
        actionwords.iClickConfirm();
        actionwords.dialogDialogIsClosed(sampleName);
        actionwords.confirmEventIsFired();
    }

    @Ignore("not implemented yet")
    @Test
    public void testReminderDialogBasicUid56bdf3b7f1a14bec8a6691ab5acd8c82() {
        reminderDialog("SampleConfirmDialog");
    }


    public void confirmDeleteDialog(String sampleName) {
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

    @Ignore("not implemented yet")
    @Test
    public void testConfirmDeleteDialogBasicUid73a333b73ac94e83a079bdb9725411ce() {
        confirmDeleteDialog("SampleConfirmDeleteDialog");
    }


    public void areYouSureYouWantToPublishDialog(String sampleName) {
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

    @Ignore("not implemented yet")
    @Test
    public void testAreYouSureYouWantToPublishDialogBasicUid4eb6bd3dd8bc4324b05c1d96c4972ca8() {
        areYouSureYouWantToPublishDialog("SampleConfirmPublishDialog");
    }


    public void unsavedChangesDialog(String sampleName) {
        actionwords.iHaveSampleDialog(sampleName);
        actionwords.iOpenDialogDialog(sampleName);
        actionwords.iCompareTheDialogToReferenceImage(sampleName);
        actionwords.iClickConfirm();
        actionwords.dialogDialogIsClosed(sampleName);
        actionwords.confirmEventIsFired();
        actionwords.iOpenDialogDialog("sample_name");
        actionwords.iClickReject();
        actionwords.dialogDialogIsClosed(sampleName);
        actionwords.rejectEventIsFired();
        actionwords.iOpenDialogDialog(sampleName);
        actionwords.iClickCancel();
        actionwords.dialogDialogIsClosed(sampleName);
        actionwords.cancelEventIsFired();
    }

    @Ignore("not implemented yet")
    @Test
    public void testUnsavedChangesDialogBasicUid6064e2706c3d4e8993a17c07bcc49f7f() {
        unsavedChangesDialog("SampleUnsavedChangesDialog");
    }
}
