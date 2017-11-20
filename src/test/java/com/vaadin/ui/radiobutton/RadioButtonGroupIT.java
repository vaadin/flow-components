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
package com.vaadin.ui.radiobutton;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.demo.ComponentDemoTest;
import com.vaadin.testbench.By;

public class RadioButtonGroupIT extends ComponentDemoTest {

    @Override
    protected String getTestPath() {
        return "/vaadin-radio-button";
    }

    @Test
    public void valueChange() {
        WebElement valueDiv = layout.findElement(By.id("button-group-value"));
        WebElement group = layout
                .findElement(By.id("button-group-with-value-change-listener"));

        List<WebElement> buttons = group
                .findElements(By.tagName("vaadin-radio-button"));

        buttons.get(1).click();

        waitUntil(driver -> "Text area value changed from 'null' to 'bar'"
                .equals(valueDiv.getText()));

        buttons.get(0).click();

        waitUntil(driver -> "Text area value changed from 'bar' to 'foo'"
                .equals(valueDiv.getText()));
    }

    @Test
    public void disabledGroup() {
        WebElement group = layout.findElement(By.id("button-group-disabled"));

        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getAttribute("disabled"));
    }

}
