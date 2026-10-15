/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.confirmdialog;

import org.junit.Assert;
import org.junit.Test;

public class ConfirmDialogTest {

    @Test
    public void setAriaDescribedBy() {
        var confirmDialog = new ConfirmDialog();
        confirmDialog.setAriaDescribedBy("aria-describedby");

        Assert.assertTrue(confirmDialog.getAriaDescribedBy().isPresent());
        Assert.assertEquals("aria-describedby",
                confirmDialog.getAriaDescribedBy().get());

        confirmDialog.setAriaDescribedBy(null);
        Assert.assertTrue(confirmDialog.getAriaDescribedBy().isEmpty());
    }

    @Test
    public void setWidth_getWidth() {
        var confirmDialog = new ConfirmDialog();
        confirmDialog.setWidth("100px");

        Assert.assertEquals("100px", confirmDialog.getWidth());
        Assert.assertEquals("100px",
                confirmDialog.getElement().getProperty("_contentWidth"));

        confirmDialog.setWidth(null);
        Assert.assertNull(confirmDialog.getWidth());
        Assert.assertNull(
                confirmDialog.getElement().getProperty("_contentWidth"));
    }

    @Test
    public void setHeight_getHeight() {
        var confirmDialog = new ConfirmDialog();
        confirmDialog.setHeight("100px");

        Assert.assertEquals("100px", confirmDialog.getHeight());
        Assert.assertEquals("100px",
                confirmDialog.getElement().getProperty("_contentHeight"));

        confirmDialog.setHeight(null);
        Assert.assertNull(confirmDialog.getHeight());
        Assert.assertNull(
                confirmDialog.getElement().getProperty("_contentHeight"));
    }
}
