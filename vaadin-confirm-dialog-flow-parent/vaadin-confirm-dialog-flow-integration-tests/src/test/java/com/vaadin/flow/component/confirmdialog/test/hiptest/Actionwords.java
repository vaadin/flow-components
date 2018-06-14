package com.vaadin.flow.component.confirmdialog.test.hiptest;

import org.junit.Assert;

import com.vaadin.flow.component.confirmdialog.examples.Features;
import com.vaadin.flow.component.confirmdialog.test.AbstractParallelTest;

public class Actionwords {

    private final AbstractParallelTest test;

    public Actionwords(AbstractParallelTest test) {
        this.test = test;
    }

    public void iClickConfirm() {
        Assert.fail("Not implemented yet");
    }

    public void confirmEventIsFired() {
        Assert.fail("Not implemented yet");
    }

    public void iOpenDialogDialog(String dialog) {
        Assert.fail("Not implemented yet");
    }

    public void iCompareTheDialogToReferenceImage(String dialog) {
        Assert.fail("Not implemented yet");
    }

    public void dialogDialogIsClosed(String dialog) {
        Assert.fail("Not implemented yet");
    }

    public void iHaveSampleDialog(String dialog) {
        test.open(Features.class, AbstractParallelTest.WINDOW_SIZE_MEDIUM);
    }

    public void iClickCancel() {
        Assert.fail("Not implemented yet");
    }

    public void cancelEventIsFired() {
        Assert.fail("Not implemented yet");
    }

    public void iClickReject() {
        Assert.fail("Not implemented yet");
    }

    public void rejectEventIsFired() {
        Assert.fail("Not implemented yet");
    }
}
