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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-multi-select-combo-box/lit-renderer")
public class MultiSelectComboBoxLitRendererIT extends AbstractComponentIT {

    private MultiSelectComboBoxElement readOnlyComboBox;

    @Before
    public void init() {
        open();
        readOnlyComboBox = $(MultiSelectComboBoxElement.class)
                .id("read-only-multi-select-combo-box");
    }

    @Test
    public void readOnly_shouldRenderSelectedItems() {
        readOnlyComboBox.openPopup();
        assertOverlayHasItem("Lit: Item 0");
        assertOverlayHasItem("Lit: Item 1");
    }

    private void assertOverlayHasItem(String name) {
        var items = getMultiSelectComboOverlayItems();
        Assert.assertTrue(
                items.stream().anyMatch(text -> text.getText().contains(name)));
    }

    private List<TestBenchElement> getMultiSelectComboOverlayItems() {
        TestBenchElement overlay = $("vaadin-multi-select-combo-box-overlay")
                .first();
        ElementQuery<TestBenchElement> items = overlay
                .$("vaadin-multi-select-combo-box-item");
        return items.all();
    }
}
