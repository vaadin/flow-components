package com.vaadin.flow.component.confirmdialog.test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BasicUseIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL() + "/basicuse");
    }

    @Test
    public void checkContent() {
        $(ButtonElement.class).first().click();
        Assert.assertEquals("My header", $(ConfirmDialogElement.class).waitForFirst().getHeaderText());
        Assert.assertEquals("Here is my text", $(ConfirmDialogElement.class).waitForFirst().getMessageText());
    }
}
