/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.checkbox.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.component.checkbox.testbench.CheckboxGroupElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-checkbox/disabled-items")
public class DisabledItemsPageIT extends AbstractComponentIT {

    @Test
    public void set_items_to_disabled_group_should_be_disabled() {
        open();
        CheckboxGroupElement group = $(CheckboxGroupElement.class)
                .id("checkbox-group");

        List<CheckboxElement> checkboxes = group.$(CheckboxElement.class).all();
        Assert.assertTrue("No buttons should be present", checkboxes.isEmpty());

        // Click button to add items
        findElement(By.id("add-button")).click();

        waitForElementPresent(By.tagName("vaadin-checkbox"));
        checkboxes = group.$(CheckboxElement.class).all();
        Assert.assertEquals("Group should have checkboxes", 2,
                checkboxes.size());

        Assert.assertTrue(
                checkboxes.stream().noneMatch(CheckboxElement::isEnabled));

    }
}
