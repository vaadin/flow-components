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
