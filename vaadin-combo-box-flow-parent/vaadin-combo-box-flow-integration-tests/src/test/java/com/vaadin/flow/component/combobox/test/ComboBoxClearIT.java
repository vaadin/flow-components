/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-combo-box/clear")
public class ComboBoxClearIT extends AbstractComboBoxIT {

    private ComboBoxElement box;

    @Test
    public void comboBoxClear() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-combo-box"))
                .size() > 0);
        box = $(ComboBoxElement.class).first();
        Assert.assertEquals(box.getSelectedText(), "Eight");
        box.clear();
        Assert.assertTrue(box.getSelectedText().isEmpty());
    }
}
