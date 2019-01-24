/*
 * Copyright 2000-2017 Vaadin Ltd.
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
import org.openqa.selenium.WebElement;

import com.vaadin.flow.demo.ComponentDemoTest;

public class DisabledItemsPageIT extends ComponentDemoTest {

    @Override
    protected String getTestPath() {
        return "/disabled-items";
    }

    @Test
    public void set_items_to_disabled_group_should_be_disabled() {
        WebElement group = layout.findElement(By.id("checkbox-group"));

        List<WebElement> checkboxes = group
                .findElements(By.tagName("vaadin-checkbox"));
        Assert.assertTrue("No buttons should be present", checkboxes.isEmpty());

        // Click button to add items
        layout.findElement(By.id("add-button")).click();

        waitForElementPresent(By.tagName("vaadin-checkbox"));
        checkboxes = group.findElements(By.tagName("vaadin-checkbox"));
        Assert.assertEquals("Group should have checkboxes", 2,
                checkboxes.size());

        // re-get the elements to not get stale element exception.
        for (WebElement button : group
                .findElements(By.tagName("vaadin-checkbox"))) {
            Assert.assertEquals("All checkboxes should be disabled",
                    Boolean.TRUE.toString(), button.getAttribute("disabled"));
        }
    }
}
