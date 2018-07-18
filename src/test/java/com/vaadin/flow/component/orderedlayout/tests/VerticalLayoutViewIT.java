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
package com.vaadin.flow.component.orderedlayout.tests;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.demo.ComponentDemoTest;

/**
 * Tests for the VerticalLayout.
 */
public class VerticalLayoutViewIT extends ComponentDemoTest {

    @Override
    protected String getTestPath() {
        return "/vaadin-ordered-layout/verticallayout";
    }

    @Test
    public void defaultLayout() {
        WebElement vLayout = layout.findElement(By.id("default-layout"));
        assertBasicFlexPropertiesAreSet(vLayout);

        Assert.assertTrue(
                "By default layout should contain spacing theme in 'theme' attribute",
                vLayout.getAttribute("theme").contains("spacing"));
        Assert.assertTrue(
                "By default layout should contain margin theme in 'theme' attribute",
                vLayout.getAttribute("theme").contains("margin"));

        checkThemeChanges(vLayout, "spacing", false);
        checkThemeChanges(vLayout, "margin", false);

        Assert.assertNull(
                "After turning off spacing and padding, layout should not contain 'theme' attribute",
                vLayout.getAttribute("theme"));

        checkThemeChanges(vLayout, "margin", true);

        checkThemeChanges(vLayout, "margin", false);

        Assert.assertNull(
                "After turning off everything, layout should not contain 'theme' attribute",
                vLayout.getAttribute("theme"));
    }

    @Test
    public void layoutWithJustifyContent() {
        WebElement vlayout = layout
                .findElement(By.id("layout-with-justify-content"));
        assertBasicFlexPropertiesAreSet(vlayout);

        Assert.assertEquals("space-between",
                vlayout.getCssValue("justify-content"));

        WebElement button = layout
                .findElement(By.id("justify-content-start-button"));
        button.click();
        Assert.assertEquals("flex-start",
                vlayout.getCssValue("justify-content"));

        button = layout.findElement(By.id("justify-content-end-button"));
        button.click();
        Assert.assertEquals("flex-end", vlayout.getCssValue("justify-content"));

        button = layout.findElement(By.id("justify-content-between-button"));
        button.click();
        Assert.assertEquals("space-between",
                vlayout.getCssValue("justify-content"));

        button = layout.findElement(By.id("justify-content-around-button"));
        button.click();
        Assert.assertEquals("space-around",
                vlayout.getCssValue("justify-content"));

        button = layout.findElement(By.id("justify-content-evenly-button"));
        button.click();
        Assert.assertEquals("space-evenly",
                vlayout.getCssValue("justify-content"));
    }

    @Test
    public void layoutWithAlignment() {
        WebElement vlayout = layout.findElement(By.id("layout-with-alignment"));
        assertBasicFlexPropertiesAreSet(vlayout);

        Assert.assertEquals("stretch", vlayout.getCssValue("align-items"));

        WebElement button = layout.findElement(By.id("align-end-button"));
        button.click();
        waitUntil(driver -> "flex-end"
                .equals(vlayout.getCssValue("align-items")));

        button = layout.findElement(By.id("align-center-button"));
        button.click();
        waitUntil(
                driver -> "center".equals(vlayout.getCssValue("align-items")));

        button = layout.findElement(By.id("align-stretch-button"));
        button.click();
        waitUntil(
                driver -> "stretch".equals(vlayout.getCssValue("align-items")));

        button = layout.findElement(By.id("align-start-button"));
        button.click();
        waitUntil(driver -> "flex-start"
                .equals(vlayout.getCssValue("align-items")));
    }

    @Test
    public void layoutWithIndividualAlignments() {
        WebElement vlayout = layout
                .findElement(By.id("layout-with-individual-alignments"));
        assertBasicFlexPropertiesAreSet(vlayout);

        Assert.assertEquals("space-between",
                vlayout.getCssValue("justify-content"));

        WebElement child = vlayout.findElement(By.id("start-aligned"));
        Assert.assertEquals("flex-start", child.getCssValue("align-self"));

        child = vlayout.findElement(By.id("center-aligned"));
        Assert.assertEquals("center", child.getCssValue("align-self"));

        child = vlayout.findElement(By.id("end-aligned"));
        Assert.assertEquals("flex-end", child.getCssValue("align-self"));

        child = vlayout.findElement(By.id("stretch-aligned"));
        Assert.assertEquals("stretch", child.getCssValue("align-self"));
    }

    @Test
    public void layoutWithExpandRatios() {
        WebElement vlayout = layout
                .findElement(By.id("layout-with-expand-ratios"));
        assertBasicFlexPropertiesAreSet(vlayout);

        WebElement child = vlayout.findElement(By.id("ratio-1"));
        Assert.assertEquals("1", child.getCssValue("flex-grow"));

        child = vlayout.findElement(By.id("ratio-2"));
        Assert.assertEquals("2", child.getCssValue("flex-grow"));

        child = vlayout.findElement(By.id("ratio-0.5"));
        Assert.assertEquals("0.5", child.getCssValue("flex-grow"));
    }

    @Test
    public void centerComponent() {
        WebElement vlayout = layout.findElement(By.id("layout-with-center"));
        assertBasicFlexPropertiesAreSet(vlayout);

        Assert.assertEquals("center",
                vlayout.getCssValue("justify-content"));
        WebElement component = layout.findElement(By.id("center"));
        Assert.assertEquals("center", component.getCssValue("align-self"));
    }

    @Test
    public void boxSizing() {
        WebElement vlayout = layout
                .findElement(By.id("vertical-layout-with-box-sizing"));
        Assert.assertEquals("border-box", vlayout.getCssValue("box-sizing"));

        WebElement button = layout.findElement(
                By.id("vertical-layout-with-box-sizing-content-box"));
        button.click();
        Assert.assertEquals("content-box", vlayout.getCssValue("box-sizing"));
    }

    private void assertBasicFlexPropertiesAreSet(WebElement vlayout) {
        Assert.assertEquals("column", vlayout.getCssValue("flex-direction"));
    }

    private void checkThemeChanges(WebElement layoutToCheck, String themeName,
            boolean shouldPresent) {
        layout.findElement(By.id(String.format("toggle-%s", themeName)))
                .click();
        if (shouldPresent) {
            waitUntil(dr -> layoutToCheck.getAttribute("theme") != null
                    && layoutToCheck.getAttribute("theme").contains(themeName));
        } else {
            waitUntil(dr -> layoutToCheck.getAttribute("theme") == null
                    || !layoutToCheck.getAttribute("theme")
                            .contains(themeName));
        }
    }
}
