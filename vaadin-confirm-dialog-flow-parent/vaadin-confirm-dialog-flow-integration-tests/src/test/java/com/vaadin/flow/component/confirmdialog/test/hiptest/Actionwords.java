package com.vaadin.flow.component.confirmdialog.test.hiptest;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.confirmdialog.test.ConfirmDialogElement;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;

import com.vaadin.flow.component.confirmdialog.examples.Features;
import com.vaadin.flow.component.confirmdialog.test.AbstractParallelTest;

import java.util.Optional;

public class Actionwords {

    private final AbstractParallelTest test;
    private final Class testView;

    public Actionwords(AbstractParallelTest test) {
        this.test = test;
        this.testView = Features.class;
    }

    public Actionwords(AbstractParallelTest test, Class testView) {
        this.test = test;
        this.testView = testView;
    }

    Optional<ConfirmDialogElement> getConfirmDialog() {
        ElementQuery<ConfirmDialogElement> query =  test.$(ConfirmDialogElement.class).onPage();
        return query.exists() ? Optional.of(query.first()) : Optional.empty();
    }

    String getEventName() {
        return test.$(TestBenchElement.class).id("eventName").getText();
    }

    public void iClickConfirm() {
        getConfirmDialog().get().getConfirmButton().click();
    }

    public void confirmEventIsFired() {
        checkFiredEventName(ConfirmDialog.ConfirmEvent.class);
    }

    public void iOpenDialogDialog(String dialog) {
        test.$(ButtonElement.class).id(dialog).click();
    }

    public void iCompareTheDialogToReferenceImage(String dialog) throws Exception {
        test.compareScreen(dialog + "opened");
    }

    public void dialogDialogIsClosed(String dialog) {
        Assert.assertFalse(getConfirmDialog().isPresent());
    }

    public void iHaveSampleDialog(String dialog) {
        test.open(testView, AbstractParallelTest.WINDOW_SIZE_MEDIUM);
    }

    public void iClickCancel() {
        getConfirmDialog().get().getCancelButton().click();
    }

    public void cancelEventIsFired() {
        checkFiredEventName(ConfirmDialog.CancelEvent.class);
    }

    public void iClickReject() {
        getConfirmDialog().get().getRejectButton().click();
    }

    public void rejectEventIsFired() {
        checkFiredEventName(ConfirmDialog.RejectEvent.class);
    }

    private void checkFiredEventName(Class eventClass) {
        Assert.assertEquals(eventClass.getSimpleName(), getEventName());
    }
}
