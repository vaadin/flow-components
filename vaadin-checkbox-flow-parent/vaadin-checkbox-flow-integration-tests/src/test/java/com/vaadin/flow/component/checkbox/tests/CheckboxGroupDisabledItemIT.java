/*
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.component.checkbox.testbench.CheckboxGroupElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

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
