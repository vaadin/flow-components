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
package com.vaadin.flow.component.dialog.tests;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("dialog-with-combo")
public class DialogWithComboBoxIT extends AbstractComponentIT {

    @Test
    public void openOverlayUsingKeybaord_overlayIsShown() {
        open();

        findElement(By.id("open-dialog")).click();

        WebElement combo = findElement(By.id("combo"));
        combo.sendKeys(Keys.ARROW_DOWN);

        WebElement info = findElement(By.id("info"));
        waitUntil(driver -> info.getText().equals(Boolean.TRUE.toString()));

        Assert.assertTrue(findElement(By.tagName("vaadin-combo-box-overlay"))
                .isDisplayed());
    }

    @Test
    public void openOverlayUsingMouse_overlayIsShown() {
        open();

        findElement(By.id("open-dialog")).click();

        WebElement combo = findElement(By.id("combo"));
        getInShadowRoot(combo, By.id("toggleButton")).click();

        WebElement info = findElement(By.id("info"));
        waitUntil(driver -> info.getText().equals(Boolean.TRUE.toString()));

        Assert.assertTrue(findElement(By.tagName("vaadin-combo-box-overlay"))
                .isDisplayed());
    }

}
