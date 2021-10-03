/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.flow.component.combobox.test;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.NoSuchElementException;

import com.vaadin.flow.testutil.TestPath;

import static org.junit.Assert.assertTrue;

@TestPath("vaadin-combo-box/readonly-blur")
public class ComboBoxReadOnlyBlurIT extends AbstractComboBoxIT {

    @Test(expected = NoSuchElementException.class)
    public void comboBoxReadOnlyBlur() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-combo-box"))
                .size() > 0);
        // Clicking button will blur the ComboBox.
        findElement(By.id("blur-combo")).click();
        // Wait until click processed.
        waitUntil(driver -> findElements(By.id("button-clicked"))
                .size() > 0);
        // Blur should trigger custom value set event.
        WebElement span = findElement(By.id("triggered"));
        assertTrue(span == null);
    }
}
