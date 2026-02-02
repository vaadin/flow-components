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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-multi-select-combo-box/i18n")
public class MultiSelectComboBoxI18nIT extends AbstractComponentIT {
    private MultiSelectComboBoxElement comboBox;

    @Before
    public void init() {
        open();
        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();
    }

    @Test
    public void setI18n_i18nIsApplied() {
        clickElementWithJs("set-i18n");

        // Select an item and verify announcement
        comboBox.openPopup();
        comboBox.waitForLoadingFinished();
        comboBox.$("vaadin-multi-select-combo-box-item").first().click();

        String selectedAnnouncement = waitForAnnouncement();
        Assert.assertTrue(
                "Announcement should contain custom selected text: "
                        + selectedAnnouncement,
                selectedAnnouncement.contains("Custom selected"));
        Assert.assertTrue(
                "Announcement should contain custom total text: "
                        + selectedAnnouncement,
                selectedAnnouncement.contains("Custom total"));

        // Clear selection and verify announcement
        comboBox.closePopup();
        comboBox.$("[part~='clear-button']").first().click();

        String clearedAnnouncement = waitForAnnouncement();
        Assert.assertEquals("Custom cleared", clearedAnnouncement);
    }

    @Test
    public void setEmptyI18n_defaultI18nIsPreserved() {
        clickElementWithJs("set-empty-i18n");

        // Select an item and verify default announcement
        comboBox.openPopup();
        comboBox.waitForLoadingFinished();
        comboBox.$("vaadin-multi-select-combo-box-item").first().click();

        String selectedAnnouncement = waitForAnnouncement();
        Assert.assertTrue(
                "Announcement should contain default selected text: "
                        + selectedAnnouncement,
                selectedAnnouncement.contains("added to selection"));
        Assert.assertTrue(
                "Announcement should contain default total text: "
                        + selectedAnnouncement,
                selectedAnnouncement.contains("items selected"));

        // Clear selection and verify default announcement
        comboBox.closePopup();
        comboBox.$("[part~='clear-button']").first().click();

        String clearedAnnouncement = waitForAnnouncement();
        Assert.assertEquals("Selection cleared", clearedAnnouncement);
    }

    /**
     * Waits for and returns the text from the aria-live announcement region.
     * The announce function has a 150ms delay before setting text.
     */
    private String waitForAnnouncement() {
        waitUntil(driver -> {
            var regions = driver.findElements(By.cssSelector("div[aria-live]"));
            return regions.stream().anyMatch(
                    r -> r.getText() != null && !r.getText().isEmpty());
        }, 2);
        return getDriver().findElement(By.cssSelector("div[aria-live]"))
                .getText();
    }
}
