/*
 * Copyright 2000-2025 Vaadin Ltd.
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
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-multi-select-combo-box/refresh-value")
public class MultiSelectComboBoxRefreshValueIT extends AbstractComponentIT {
    private MultiSelectComboBoxElement comboBox;
    private TestBenchElement changeItemLabelGenerator;
    private TestBenchElement changeItemData;

    @Before
    public void init() {
        open();
        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();
        changeItemLabelGenerator = $("button")
                .id("change-item-label-generator");
        changeItemData = $("button").id("change-item-data");
    }

    @Test
    public void selectItems_changeItemLabelGenerator_valueUpdated() {
        comboBox.selectByText("Item 1");
        comboBox.selectByText("Item 10");
        changeItemLabelGenerator.click();

        assertSelectedItems(Set.of("Custom Item 1", "Custom Item 10"));
    }

    @Test
    @Ignore("https://github.com/vaadin/flow-components/issues/3239")
    public void selectItems_changeItemData_valueUpdated() {
        comboBox.selectByText("Item 1");
        comboBox.selectByText("Item 10");
        changeItemData.click();

        assertSelectedItems(Set.of("Updated Item 1", "Updated Item 10"));
    }

    private void assertSelectedItems(Set<String> items) {
        List<String> selectedTexts = comboBox.getSelectedTexts();
        Assert.assertEquals("Number of selected items does not match",
                items.size(), selectedTexts.size());
        items.forEach(item -> Assert.assertTrue(
                "Selection does not include item: " + item,
                selectedTexts.contains(item)));
    }
}
