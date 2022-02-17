/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-radio-button/radio-button-group-required-binder")
public class RequiredValidationIT extends AbstractComponentIT {

    @Test
    public void requiredValidation_disabledWithBinder_enabledViaExpicitCall()
            throws InterruptedException {
        open();

        WebElement group = findElement(By.id("gender"));
        $("vaadin-radio-button").first().sendKeys(Keys.TAB);
        Assert.assertFalse("Radio button group should be valid.",
                Boolean.parseBoolean(group.getAttribute("invalid")));

        findElement(By.id("hide")).click();

        Assert.assertTrue("Radio button group should be invalid",
                Boolean.parseBoolean(group.getAttribute("invalid")));
    }

    @Test
    public void groupWithInvalidOption() {
        open();

        WebElement group = findElement(
                By.id("radio-button-with-invalid-option"));
        WebElement radioButton = group
                .findElements(By.tagName("vaadin-radio-button")).get(2);

        Assert.assertFalse("Radio button group should be valid.",
                Boolean.parseBoolean(group.getAttribute("invalid")));
        radioButton.click();

        Assert.assertTrue("Radio button group should be invalid.",
                Boolean.parseBoolean(group.getAttribute("invalid")));

        radioButton.sendKeys(Keys.TAB);
        Assert.assertTrue("Radio button group should keep invalid.",
                Boolean.parseBoolean(group.getAttribute("invalid")));
    }

    @Test
    public void groupInvalidOnAttach() {
        open();

        WebElement group = findElement(By.id("radio-button-invalid-on-attach"));

        Assert.assertTrue("Radio button group should be invalid.",
                Boolean.parseBoolean(group.getAttribute("invalid")));
    }

}
