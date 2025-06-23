/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-combo-box/lazy-combo-box-filter")
public class LazyComboBoxFilterIT extends AbstractComponentIT {

    @Test
    public void lazyComboBoxFilterFirstQuery() {
        open();
        ComboBoxElement comboBox = $(ComboBoxElement.class).first();
        comboBox.sendKeys("1");
        comboBox.openPopup();

        WebElement query = findElement(By.id("query"));
        Assert.assertTrue(query.getText().contains("Filter: 1"));
        Assert.assertTrue(query.getText().contains("Count: 10"));
    }
}
