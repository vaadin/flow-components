/**
 * Copyright 2000-2024 Vaadin Ltd.
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
}
