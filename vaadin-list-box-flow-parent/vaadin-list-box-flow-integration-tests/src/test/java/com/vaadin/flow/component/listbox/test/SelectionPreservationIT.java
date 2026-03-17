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
package com.vaadin.flow.component.listbox.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.listbox.testbench.ListBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-list-box/selection-preservation")
public class SelectionPreservationIT extends AbstractComponentIT {

    private ListBoxElement listBox;

    @Before
    public void init() {
        open();
        listBox = $(ListBoxElement.class).single();
    }

    @Test
    public void preserveAll_selectMultiple_refreshAll_selectionPreserved() {
        clickElementWithJs("mode-preserve-all");

        clickItem(4);
        clickItem(5);
        clickItem(6);

        clickElementWithJs("refresh-all");

        Assert.assertEquals(List.of("4", "5", "6"), getSelectedItemTexts());
    }

    @Test
    public void preserveExisting_selectMultiple_refreshAll_selectionPreserved() {
        clickElementWithJs("mode-preserve-existing");

        clickItem(2);
        clickItem(7);

        clickElementWithJs("refresh-all");

        Assert.assertEquals(List.of("2", "7"), getSelectedItemTexts());
    }

    @Test
    public void preserveAll_removeSelectedItem_refreshAll_serverValueStillContainsRemovedItem() {
        clickElementWithJs("mode-preserve-all");

        clickItem(4);
        clickItem(5);
        clickItem(6);

        clickElementWithJs("remove-item-5");
        clickElementWithJs("refresh-all");

        Assert.assertEquals("4,5,6", getServerValue());
        Assert.assertEquals(List.of("4", "6"), getSelectedItemTexts());
    }

    @Test
    public void preserveExisting_removeSelectedItem_refreshAll_removedItemDeselected() {
        clickElementWithJs("mode-preserve-existing");

        clickItem(4);
        clickItem(5);
        clickItem(6);

        clickElementWithJs("remove-item-5");
        clickElementWithJs("refresh-all");

        Assert.assertEquals("4,6", getServerValue());
        Assert.assertEquals(List.of("4", "6"), getSelectedItemTexts());
    }

    @Test
    public void discard_selectMultiple_refreshAll_selectionCleared() {
        clickElementWithJs("mode-discard");

        clickItem(1);
        clickItem(3);

        clickElementWithJs("refresh-all");

        Assert.assertTrue(getSelectedItemTexts().isEmpty());
    }

    private String getServerValue() {
        clickElementWithJs("show-server-value");
        return $("span").id("server-value").getText();
    }

    private void clickItem(int index) {
        getAllItems().get(index).click();
    }

    private List<TestBenchElement> getAllItems() {
        return listBox.$("vaadin-item").all();
    }

    private List<String> getSelectedItemTexts() {
        return getAllItems().stream()
                .filter(item -> item.hasAttribute("selected"))
                .map(TestBenchElement::getText).toList();
    }
}
