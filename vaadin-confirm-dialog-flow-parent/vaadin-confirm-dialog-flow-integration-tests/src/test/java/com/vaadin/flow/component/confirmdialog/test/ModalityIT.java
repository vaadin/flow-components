package com.vaadin.flow.component.confirmdialog.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.tests.AbstractParallelTest;

import static com.vaadin.flow.component.confirmdialog.examples.BasicUseView.Log.LOG_ID;

@TestPath("vaadin-confirm-dialog/modality")
public class ModalityIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        Assert.assertEquals("No log messages should exist on open", 0,
                $(DivElement.class).id(LOG_ID).$("div").all().size());
    }

    @Test
    public void openConfirmDialog_removeBackdrop_logClickNotAccepted() {
        $(ButtonElement.class).id("open-dialog").click();
        final DivElement backdrop = $(TestBenchElement.class).id("overlay")
                .$(DivElement.class).id("backdrop");

        executeScript("arguments[0].remove()", backdrop);

        Assert.assertFalse("Backdrop was not removed from dom",
                $(TestBenchElement.class).id("overlay").$(DivElement.class)
                        .attributeContains("id", "backdrop").exists());

        $(ButtonElement.class).id("log").click();

        Assert.assertTrue("Dialog should not have closed",
                $(ConfirmDialogElement.class).exists());
        Assert.assertEquals("Click on button should not generate a log message",
                0, $(DivElement.class).id(LOG_ID).$("div").all().size());

        $(ConfirmDialogElement.class).first().getConfirmButton().click();

        Assert.assertFalse("Dialog should have closed",
                $(ConfirmDialogElement.class).exists());

        $(ButtonElement.class).id("log").click();

        Assert.assertEquals("Click should have resulted in a log message", 1,
                $(DivElement.class).id(LOG_ID).$("div").all().size());
    }

}
