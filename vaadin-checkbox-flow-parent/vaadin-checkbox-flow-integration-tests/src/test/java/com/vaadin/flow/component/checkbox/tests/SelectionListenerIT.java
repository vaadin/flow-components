/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.checkbox.tests;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-checkbox/selection-listener")
public class SelectionListenerIT extends AbstractComponentIT {

    @Test
    public void selectItem_selectionEventIsFired() {
        open();

        $("vaadin-checkbox-group").first().$(CheckboxElement.class).first()
                .click();

        WebElement selection = findElement(By.id("current-selection"));
        Assert.assertEquals("foo", selection.getText());
    }
}
