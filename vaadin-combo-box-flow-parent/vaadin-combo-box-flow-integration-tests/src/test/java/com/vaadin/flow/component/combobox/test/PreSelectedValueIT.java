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

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-combo-box/pre-selected")
public class PreSelectedValueIT extends AbstractComponentIT {

    @Test
    public void selectedValueIsNotResetAfterClientResponse() {
        open();

        findElement(By.id("get-value")).click();

        WebElement info = $("div").id("info");
        Assert.assertEquals("Item 1", info.getText());
    }
}
