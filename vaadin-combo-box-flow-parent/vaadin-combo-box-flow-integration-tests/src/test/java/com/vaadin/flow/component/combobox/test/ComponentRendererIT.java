/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-combo-box/component-renderer")
public class ComponentRendererIT extends AbstractComboBoxIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void captionsForItemsExistWhenFirstAddingItems() {
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id("before-renderer");

        testItems(comboBox);

    }

    @Test
    public void captionsForItemsExistWhenFirstAddingRenderer() {
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id("after-renderer");

        testItems(comboBox);
    }

    @Test
    public void captionsForItemsExistWhenFirstSettingDataProvider() {
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id("dp-before-renderer");

        testItems(comboBox);

    }

    @Test
    public void captionsForItemsExistWhenFirstAddingRenderer_thenDataProvider() {
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id("dp-after-renderer");

        testItems(comboBox);
    }

    @Test
    public void multiplePagesOfItems_scrollDown_close_noItemsWhenReopened() {
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id("multiple-pages-of-items");

        comboBox.openPopup();
        waitUntilTextInContent("Song");

        for (int i = 0; i < 600; i += 50) {
            scrollToItem(comboBox, i);
        }

        comboBox.closePopup();

        String firstItemText = (String) executeScript("arguments[0].open();"
                + "const item = document.querySelector('vaadin-combo-box-item');"
                + "const spanInItem = item.querySelector('span');"
                + "return spanInItem ? spanInItem.innerText : '';", comboBox);
        Assert.assertEquals("", firstItemText);
    }

    private void testItems(TestBenchElement comboBox) {
        executeScript("arguments[0].open(); return true;", comboBox);
        TestBenchElement overlay = $("vaadin-combo-box-overlay").first();
        ElementQuery<TestBenchElement> items = overlay
                .$("vaadin-combo-box-item");

        Assert.assertEquals("ComboBox should always contain 3 items", 3,
                items.all().size());

        items.all().forEach(item -> Assert.assertTrue(
                "Component renderer not run as we have no VerticalLayout.",
                item.$(VerticalLayoutElement.class).exists()));
    }
}
