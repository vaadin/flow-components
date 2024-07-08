/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.listbox.test;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-list-box/list-box-retain-value")
public class ListBoxRetainValueIT extends AbstractComponentIT {

    @Test
    public void listBoxRetainValueWhenRemovedAndAdded() {
        open();
        WebElement value = findElement(By.id("list-box-value"));
        Assert.assertEquals(value.getText(), "2");
        findElement(By.id("add-button")).click();
        Assert.assertEquals(value.getText(), "2");
    }
}
