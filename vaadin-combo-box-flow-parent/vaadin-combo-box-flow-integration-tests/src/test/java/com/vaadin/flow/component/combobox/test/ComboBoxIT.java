/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("/vaadin-combo-box-test-demo")
public class ComboBoxIT extends AbstractComboBoxIT {

    @Before
    public void init() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-combo-box"))
                .size() > 0);
    }

    @Test
    public void openStringBoxAndSelectAnItem() {
        checkLogsForErrors();
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id("string-selection-box");
        WebElement message = findElement(By.id("string-selection-message"));
        Assert.assertEquals("Input browser name", comboBox.getPlaceholder());

        comboBox.selectByText("Opera");

        Assert.assertEquals("Selected browser: Opera", message.getText());
    }

    @Test
    public void openObjectBoxAndSelectAnItem() {
        checkLogsForErrors();
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id("object-selection-box");
        WebElement message = findElement(By.id("object-selection-message"));

        comboBox.selectByText("Sculpted");

        waitUntil(driver -> message.getText().equals(
                "Selected song: Sculpted\nFrom album: Two Fold Pt.1\nBy artist: Haywyre"));
    }

    @Test
    public void openValueBoxSelectTwoItems() {
        checkLogsForErrors();
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id("value-selection-box");
        WebElement message = findElement(By.id("value-selection-message"));

        comboBox.selectByText("Haywyre");

        waitUntil(
                driver -> message.getText().equals("Selected artist: Haywyre"));

        comboBox.selectByText("Haircuts for Men");

        waitUntil(driver -> message.getText().equals(
                "Selected artist: Haircuts for Men\nThe old selection was: Haywyre"));
    }

    @Test
    public void openTemplateBox() {
        checkLogsForErrors();
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id("template-selection-box");

        comboBox.openPopup();

        Assert.assertEquals("A V Club Disagrees", comboBox.getOptions().get(0));
        Assert.assertEquals("A V Club Disagrees\nHaircuts for Men",
                getItemElements(comboBox).get(0).getText());
    }

    @Test
    public void templateBoxCustomFiltering_filterableByArtist() {
        checkLogsForErrors();
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id("template-selection-box");
        comboBox.setFilter("ha");

        Assert.assertEquals(List.of("A V Club Disagrees", "Sculpted"),
                comboBox.getOptions());

        List<TestBenchElement> items = getItemElements(comboBox);
        Assert.assertEquals("A V Club Disagrees\nHaircuts for Men",
                items.get(0).getText());
        Assert.assertEquals("Sculpted\nHaywyre", items.get(1).getText());
    }
}
