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
package com.vaadin.flow.component.radiobutton.tests;

import java.util.List;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-radio-button/disabled-items")
public class DisabledItemsPageIT extends AbstractComponentIT {

    @Test
    public void set_items_to_disabled_group_should_be_disabled() {
        open();
        WebElement group = findElement(By.id("button-group"));

        List<WebElement> buttons = group
                .findElements(By.tagName("vaadin-radio-button"));
        Assert.assertTrue("No buttons should be present", buttons.isEmpty());

        // Click button to add items
        findElement(By.id("add-button")).click();

        waitForElementPresent(By.tagName("vaadin-radio-button"));
        buttons = group.findElements(By.tagName("vaadin-radio-button"));
        Assert.assertEquals("Group should have buttons", 2, buttons.size());

        // re-get the elements to not get stale element exception.
        for (WebElement button : group.findElements(By.tagName("vaadin-radio-button"))) {
            Assert.assertEquals("All buttons should be disabled",
                    Boolean.TRUE.toString(), button.getAttribute("disabled"));
        }
    }
}
