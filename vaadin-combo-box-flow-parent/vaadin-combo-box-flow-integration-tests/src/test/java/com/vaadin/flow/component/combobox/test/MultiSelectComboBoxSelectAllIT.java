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

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-multi-select-combo-box/select-all")
public class MultiSelectComboBoxSelectAllIT extends AbstractComponentIT {
    private MultiSelectComboBoxElement serverSideComboBox;
    private MultiSelectComboBoxElement clientSideComboBox;

    @Before
    public void init() {
        open();
        serverSideComboBox = $(MultiSelectComboBoxElement.class)
                .id("server-side");
        clientSideComboBox = $(MultiSelectComboBoxElement.class)
                .id("client-side");
    }

    @Test
    public void serverSide_selectAll_selectsAllItems() {
        clickSelectAll(serverSideComboBox);

        assertSelectedCount(serverSideComboBox, 100);
        assertValueChangeEvent("server-side", 1, 100);

        // Items must be sent with the labels the server generates for them
        List<String> selectedTexts = serverSideComboBox.getSelectedTexts();
        Assert.assertEquals("Item 1", selectedTexts.get(0));
        Assert.assertEquals("Item 100", selectedTexts.get(99));

        assertSelectAllLabel(serverSideComboBox, "Deselect All");
    }

    @Test
    public void serverSide_selectAll_announcesTotal() {
        clickSelectAll(serverSideComboBox);

        waitForAnnouncement("100 items selected");
    }

    @Test
    public void serverSide_selectAll_deselectAll_clearsSelection() {
        clickSelectAll(serverSideComboBox);
        assertSelectedCount(serverSideComboBox, 100);
        assertSelectAllLabel(serverSideComboBox, "Deselect All");

        clickSelectAll(serverSideComboBox);

        assertSelectedCount(serverSideComboBox, 0);
        assertValueChangeEvent("server-side", 2, 0);
        assertSelectAllLabel(serverSideComboBox, "Select All");
        waitForAnnouncement("Selection cleared");
    }

    @Test
    public void serverSide_filter_selectFiltered_selectsMatchingItems() {
        serverSideComboBox.setFilter("Item 10");
        assertSelectAllLabel(serverSideComboBox, "Select Filtered");

        clickSelectAll(serverSideComboBox);

        assertSelectedCount(serverSideComboBox, 2);
        assertValueChangeEvent("server-side", 1, 2);
        List<String> selectedTexts = serverSideComboBox.getSelectedTexts();
        Assert.assertTrue(selectedTexts.contains("Item 10"));
        Assert.assertTrue(selectedTexts.contains("Item 100"));
        assertSelectAllLabel(serverSideComboBox, "Deselect Filtered");
    }

    @Test
    public void serverSide_selectAll_filter_deselectFiltered_keepsOtherItems() {
        clickSelectAll(serverSideComboBox);
        assertSelectedCount(serverSideComboBox, 100);

        serverSideComboBox.setFilter("Item 10");
        assertSelectAllLabel(serverSideComboBox, "Deselect Filtered");

        clickSelectAll(serverSideComboBox);

        assertSelectedCount(serverSideComboBox, 98);
        assertValueChangeEvent("server-side", 2, 98);
        List<String> selectedTexts = serverSideComboBox.getSelectedTexts();
        Assert.assertFalse(selectedTexts.contains("Item 10"));
        Assert.assertFalse(selectedTexts.contains("Item 100"));
        Assert.assertTrue(selectedTexts.contains("Item 1"));
    }

    @Test
    public void clientSide_selectAll_selectsAllItems() {
        clickSelectAll(clientSideComboBox);

        assertSelectedCount(clientSideComboBox, 10);
        assertValueChangeEvent("client-side", 1, 10);
        assertSelectAllLabel(clientSideComboBox, "Deselect All");
    }

    @Test
    public void clientSide_selectAll_deselectAll_clearsSelection() {
        clickSelectAll(clientSideComboBox);
        assertSelectedCount(clientSideComboBox, 10);
        assertSelectAllLabel(clientSideComboBox, "Deselect All");

        clickSelectAll(clientSideComboBox);

        assertSelectedCount(clientSideComboBox, 0);
        assertValueChangeEvent("client-side", 2, 0);
        assertSelectAllLabel(clientSideComboBox, "Select All");
    }

    @Test
    public void clientSide_filter_selectFiltered_selectsMatchingItems() {
        clientSideComboBox.setFilter("Item 1");
        assertSelectAllLabel(clientSideComboBox, "Select Filtered");

        clickSelectAll(clientSideComboBox);

        assertSelectedCount(clientSideComboBox, 2);
        assertValueChangeEvent("client-side", 1, 2);
        List<String> selectedTexts = clientSideComboBox.getSelectedTexts();
        Assert.assertTrue(selectedTexts.contains("Item 1"));
        Assert.assertTrue(selectedTexts.contains("Item 10"));
    }

    @Test
    public void switchToClientSideFilter_selectAll_selectsAllItems() {
        clickElementWithJs("set-few-items");

        clickSelectAll(serverSideComboBox);

        assertSelectedCount(serverSideComboBox, 10);
        assertValueChangeEvent("server-side", 1, 10);
        assertSelectAllLabel(serverSideComboBox, "Deselect All");
    }

    @Test
    public void getSelectAllButton_popupClosed_throws() {
        Assert.assertThrows(IllegalStateException.class,
                () -> serverSideComboBox.getSelectAllButton());
    }

    @Test
    public void getSelectAllButton_noMatchingItems_returnsNull() {
        serverSideComboBox.setFilter("foo");

        Assert.assertNull(serverSideComboBox.getSelectAllButton());
    }

    private TestBenchElement getSelectAllButton(
            MultiSelectComboBoxElement comboBox) {
        comboBox.openPopup();
        return comboBox.getSelectAllButton();
    }

    private void clickSelectAll(MultiSelectComboBoxElement comboBox) {
        getSelectAllButton(comboBox).click();
    }

    private void assertSelectAllLabel(MultiSelectComboBoxElement comboBox,
            String label) {
        Assert.assertEquals(label, getSelectAllButton(comboBox).getText());
    }

    private void assertSelectedCount(MultiSelectComboBoxElement comboBox,
            int count) {
        Assert.assertEquals(count, comboBox.getSelectedTexts().size());
    }

    private void assertValueChangeEvent(String id, int expectedEventCount,
            int expectedValueSize) {
        Assert.assertEquals(String.valueOf(expectedEventCount),
                $("span").id(id + "-event-count").getText());
        Assert.assertEquals(String.valueOf(expectedValueSize),
                $("span").id(id + "-event-value-size").getText());
        Assert.assertEquals("client",
                $("span").id(id + "-event-origin").getText());
    }

    private void waitForAnnouncement(String expectedText) {
        waitUntil(driver -> driver
                .findElements(By.cssSelector("div[aria-live]")).stream()
                .anyMatch(region -> expectedText.equals(region.getText())));
    }
}
