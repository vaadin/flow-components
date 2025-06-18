/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.checkbox.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.component.checkbox.testbench.CheckboxGroupElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-checkbox-group-disabled-item")
public class CheckboxGroupDisabledItemIT extends AbstractComponentIT {

    @Test
    public void disabledGroupItemChecked() {
        open();
        CheckboxGroupElement group = $(CheckboxGroupElement.class)
                .id("checkbox-group-disabled-item");

        List<CheckboxElement> checkboxes = group.getCheckboxes();

        Assert.assertTrue(checkboxes.get(1).isChecked());
    }

    @Test
    public void disabledItemCanBeCheckedProgrammatically() {
        open();
        CheckboxGroupElement group = $(CheckboxGroupElement.class)
                .id("checkbox-group-disabled-item");
        List<CheckboxElement> checkboxes = group.getCheckboxes();
        CheckboxElement secondCheckbox = checkboxes.get(1);
        TestBenchElement toggleBarButton = $("button").id("toggle-bar-button");

        // Deselect
        toggleBarButton.click();
        Assert.assertFalse(secondCheckbox.isChecked());

        // Reselect
        toggleBarButton.click();
        Assert.assertTrue(secondCheckbox.isChecked());
    }

    /**
     * Regression test for:
     * https://github.com/vaadin/flow-components/issues/1185
     */
    @Test
    public void enabledItemCanBeCheckedManuallyWhenSettingItemEnabledProviderAfterSelectingValue() {
        open();
        CheckboxGroupElement group = $(CheckboxGroupElement.class)
                .id("checkbox-group-disabled-item");
        List<CheckboxElement> checkboxes = group.getCheckboxes();
        CheckboxElement firstCheckbox = checkboxes.get(0);

        // Select
        firstCheckbox.click();
        Assert.assertTrue(firstCheckbox.isChecked());

        // Deselect
        firstCheckbox.click();
        Assert.assertFalse(firstCheckbox.isChecked());
    }

    @Test
    public void enablingTheGroupDoesNotEnableItemDisabledWithItemEnabledProvider() {
        open();
        CheckboxGroupElement group = $(CheckboxGroupElement.class)
                .id("checkbox-group-disabled-item");
        List<CheckboxElement> checkboxes = group.getCheckboxes();
        TestBenchElement toggleEnabledButton = $("button")
                .id("toggle-enabled-button");

        // Disable group
        toggleEnabledButton.click();

        // Re-enable group
        toggleEnabledButton.click();

        Assert.assertEquals("Second checkbox should be disabled",
                Boolean.TRUE.toString(),
                checkboxes.get(1).getAttribute("disabled"));
    }

}
