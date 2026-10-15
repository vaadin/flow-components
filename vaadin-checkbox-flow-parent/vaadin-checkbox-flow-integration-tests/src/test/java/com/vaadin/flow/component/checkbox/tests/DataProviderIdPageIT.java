/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.checkbox.tests;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.component.checkbox.testbench.CheckboxGroupElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-checkbox/data-provider-id")
public class DataProviderIdPageIT extends AbstractComponentIT {

    @Test
    public void selectById_itemsAreNotEqualButHasSameId_itemIsSelected() {
        open();

        findElement(By.id("select-by-id")).click();

        CheckboxElement barCheckbox = $(CheckboxGroupElement.class)
                .id("id-data-provider").$(CheckboxElement.class).all().get(1);
        Assert.assertTrue(barCheckbox.isChecked());
    }

    @Test
    public void selectByEquals_itemsAreEqual_itemIsSelected() {
        open();

        findElement(By.id("select-by-equals")).click();

        CheckboxElement barCheckbox = $(CheckboxGroupElement.class)
                .id("standard-equals").$(CheckboxElement.class).all().get(1);
        Assert.assertTrue(barCheckbox.isChecked());
    }

    @Test
    public void selectById_itemsAreNotEqualButHasSameId_itemIsNotSelected() {
        open();

        findElement(By.id("no-selection")).click();

        CheckboxElement barCheckbox = $(CheckboxGroupElement.class)
                .id("id-data-provider").$(CheckboxElement.class).all().get(1);
        Assert.assertFalse(barCheckbox.isChecked());
    }
}
