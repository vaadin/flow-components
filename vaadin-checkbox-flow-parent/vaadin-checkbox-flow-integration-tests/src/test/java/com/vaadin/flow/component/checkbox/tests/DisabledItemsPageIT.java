/*
 * Copyright 2000-2025 Vaadin Ltd.
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
