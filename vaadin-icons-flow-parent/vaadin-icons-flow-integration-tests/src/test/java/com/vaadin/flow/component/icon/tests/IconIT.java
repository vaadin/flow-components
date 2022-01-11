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
package com.vaadin.flow.component.icon.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.icon.demo.IconView;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the {@link IconView}.
 */
@TestPath("vaadin-icons/icon-view")
public class IconIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void basicIcons() {
        assertIconProperty("close-icon", "vaadin", "close");
        assertIconProperty("clock-icon", "lumo", "clock");
    }

    @Test
    public void styledIcon() {
        WebElement icon = findElement(By.id("logo-icon"));
        assertIconProperty(icon, "vaadin", "vaadin-h");

        assertCssValue(icon, "width", "100px");
        assertCssValue(icon, "height", "100px");

        assertCssValue(icon, "fill", "rgb(255, 165, 0)");
    }

    @Test
    public void clickableIcon() {
        WebElement message = findElement(By.id("clickable-message"));
        Assert.assertEquals("", message.getText());

        WebElement icon = findElement(By.id("clickable-v-icon"));
        icon.click();

        Assert.assertEquals("The VAADIN_V icon was clicked!",
                message.getText());

        icon = findElement(By.id("clickable-h-icon"));
        icon.click();

        Assert.assertEquals("The VAADIN_H icon was clicked!",
                message.getText());
    }

    @Test
    public void allAvailableIcons() {
        WebElement allIcons = findElement(By.id("all-icons"));
        List<WebElement> labels = allIcons.findElements(By.tagName("label"));
        List<WebElement> icons = allIcons
                .findElements(By.tagName("vaadin-icon"));

        Assert.assertEquals(VaadinIcon.values().length, labels.size());
        Assert.assertEquals(VaadinIcon.values().length, icons.size());

        for (int i = 0; i < labels.size(); i += 17) {
            WebElement label = labels.get(i);
            WebElement icon = icons.get(i);
            String enumName = VaadinIcon.values()[i].name();
            Assert.assertEquals(enumName, label.getText());
            assertIconProperty(icon, "vaadin",
                    enumName.toLowerCase().replace('_', '-'));
        }
    }

    private void assertIconProperty(String id, String collection,
            String iconName) {
        assertIconProperty(findElement(By.id(id)), collection, iconName);
    }

    private void assertIconProperty(WebElement icon, String collection,
            String iconName) {
        Assert.assertEquals(collection + ":" + iconName,
                icon.getAttribute("icon"));
    }

    private void assertCssValue(WebElement element, String propertyName,
            String expectedValue) {
        Assert.assertEquals(expectedValue, element.getCssValue(propertyName));
    }
}
