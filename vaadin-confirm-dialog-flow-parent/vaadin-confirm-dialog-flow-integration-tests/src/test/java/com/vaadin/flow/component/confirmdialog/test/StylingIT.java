package com.vaadin.flow.component.confirmdialog.test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-confirm-dialog/styling")
public class StylingIT extends AbstractComponentIT {

    private ButtonElement addDialog;
    private ButtonElement openDialog;
    private ButtonElement addFoo;

    @Before
    public void init() {
        open();
        addDialog = $(ButtonElement.class).id("add-dialog");
        openDialog = $(ButtonElement.class).id("open-dialog");
        addFoo = $(ButtonElement.class).id("add-foo");
    }

    @Test
    public void addClassBeforeAdd() {
        addFoo.click();

        addDialog.click();
        openDialog.click();

        String value = getOverlayClassName();
        Assert.assertEquals("foo", value);
    }

    @Test
    public void addClassBeforeOpen() {
        addFoo.click();

        openDialog.click();

        String value = getOverlayClassName();
        Assert.assertEquals("foo", value);
    }

    @Test
    public void addClassAfterOpen() {
        openDialog.click();

        $(ButtonElement.class).id("set-bar").click();

        String value = getOverlayClassName();
        Assert.assertEquals("bar", value);
    }

    @Test
    public void removeClassAfterOpen() {
        addFoo.click();
        openDialog.click();

        $(ButtonElement.class).id("remove-all").click();

        String value = getOverlayClassName();
        Assert.assertEquals("", value);
    }

    private ConfirmDialogElement getConfirmDialog() {
        return $(ConfirmDialogElement.class).waitForFirst();
    }

    private TestBenchElement getOverlay() {
        return ((TestBenchElement) getConfirmDialog().getContext());
    }

    private String getOverlayClassName() {
        return getOverlay().getAttribute("class");
    }
}
