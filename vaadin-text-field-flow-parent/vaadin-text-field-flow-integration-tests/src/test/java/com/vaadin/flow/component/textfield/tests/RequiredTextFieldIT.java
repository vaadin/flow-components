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
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-text-field/required-field")
public class RequiredTextFieldIT extends AbstractComponentIT {

    @Test
    public void updateServerSideValue() {
        open();

        TestBenchElement textFeild = $("vaadin-text-field").first();
        TestBenchElement input = textFeild.$("input").first();
        input.sendKeys("a");
        input.sendKeys(Keys.ENTER);
        input.sendKeys(Keys.BACK_SPACE);
        input.sendKeys(Keys.ENTER);

        WebElement message = findElement(By.id("message"));
        message.click();

        assertChanges(textFeild, message);

        input.sendKeys("a");
        while (!input.getAttribute("value").isEmpty()) {
            input.sendKeys(Keys.BACK_SPACE);
        }
        input.sendKeys(Keys.ENTER);
        message.click();

        assertChanges(textFeild, message);
    }

    private void assertChanges(TestBenchElement textFeild, WebElement message) {
        Assert.assertEquals(Boolean.TRUE.toString(),
                textFeild.getAttribute("invalid"));
        Assert.assertEquals("Value changed from 'a' to ''", message.getText());
    }
}
