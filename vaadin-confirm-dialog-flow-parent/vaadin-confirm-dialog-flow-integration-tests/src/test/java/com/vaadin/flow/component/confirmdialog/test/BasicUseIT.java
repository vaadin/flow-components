/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.confirmdialog.test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.tests.AbstractParallelTest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BasicUseIT extends AbstractParallelTest {

    @Before
    public void init() {
        String url = getBaseURL().replace(super.getBaseURL(),
                super.getBaseURL() + "/vaadin-confirm-dialog") + "/basicuse";
        getDriver().get(url);
    }

    @Test
    public void checkContent() {
        $(ButtonElement.class).first().click();
        Assert.assertEquals("My header",
                $(ConfirmDialogElement.class).waitForFirst().getHeaderText());
        Assert.assertEquals("Here is my text",
                $(ConfirmDialogElement.class).waitForFirst().getMessageText());
    }
}
