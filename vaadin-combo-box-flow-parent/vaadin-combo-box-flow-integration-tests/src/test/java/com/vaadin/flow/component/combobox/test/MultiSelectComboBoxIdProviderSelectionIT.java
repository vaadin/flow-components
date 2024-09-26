/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.screenshot.ImageFileUtil;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-multi-select-combo-box/id-provider-selection")
public class MultiSelectComboBoxIdProviderSelectionIT
        extends AbstractComponentIT {
    private MultiSelectComboBoxElement comboBox;

    @Before
    public void init() {
        open();
        driver.manage().window().setSize(new Dimension(500, 500));
        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();
    }

    @Test
    public void selectItemsServerSide_selectedItemsUpdated()
            throws IOException {
        comboBox.selectByText("abc");
        assertSelectedItems(Set.of("abc"));

        comboBox.openPopup();

        new WebDriverWait(driver, Duration.ofMillis(3000))
                .until(ExpectedConditions.visibilityOf(findElement(
                        By.tagName("vaadin-multi-select-combo-box-item"))));
        assertTrue("Screenshots differ", testBench()
                .compareScreen(ImageFileUtil.getReferenceScreenshotFile(
                        "vaadin-multi-select-combo-box_id-provider-selection.png")));
    }

    private void assertSelectedItems(Set<String> items) {
        List<String> selectedTexts = comboBox.getSelectedTexts();
        Assert.assertEquals("Number of selected items does not match",
                items.size(), selectedTexts.size());
        items.forEach(
                item -> assertTrue("Selection does not include item: " + item,
                        selectedTexts.contains(item)));
    }

}
