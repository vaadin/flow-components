/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.confirmdialog.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-confirm-dialog/basicuse")
public class BasicUseIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
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
