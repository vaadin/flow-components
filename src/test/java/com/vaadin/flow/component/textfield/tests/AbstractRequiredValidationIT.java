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
package com.vaadin.flow.component.textfield.tests;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.AbstractComponentIT;

public abstract class AbstractRequiredValidationIT extends AbstractComponentIT {

    @Test
    public void requiredValidation_disabledWithBinder_enabledViaExpicitCall()
            throws InterruptedException {
        open();

        WebElement name = findElement(By.id("name"));
        name.sendKeys(Keys.TAB);
        Assert.assertFalse(Boolean.parseBoolean(name.getAttribute("invalid")));

        findElement(By.id("hide")).click();

        name.sendKeys(Keys.TAB);

        Assert.assertTrue(Boolean.parseBoolean(name.getAttribute("invalid")));
    }

}
