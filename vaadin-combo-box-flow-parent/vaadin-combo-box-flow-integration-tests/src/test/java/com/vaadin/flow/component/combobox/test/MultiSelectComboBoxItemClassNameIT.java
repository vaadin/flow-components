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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-multi-select-combo-box/item-class-name")
public class MultiSelectComboBoxItemClassNameIT extends AbstractComponentIT {

    private MultiSelectComboBoxElement comboBox;

    @Before
    public void init() {
        open();
        comboBox = $(MultiSelectComboBoxElement.class).first();
    }

    @Test
    public void noClassesOnItemsSetInitially() {
        comboBox.openPopup();

        assertItemClassNames("", "", "");
    }

    @Test
    public void setClassNameGenerator_itemClassesGenerated() {
        click("set-generator");
        comboBox.openPopup();

        assertItemClassNames("item-foo", "item-bar", "item-baz");
    }

    @Test
    public void changeClassNameGeneratorToReturnNull_itemClassesRemoved() {
        click("set-generator");
        click("reset-generator");
        comboBox.openPopup();

        assertItemClassNames("", "", "");
    }

    @Test
    public void setValue_noClassesOnChipsSetInitially() {
        click("set-value");

        assertChipClassNames("", "");
    }

    @Test
    public void setClassNameGenerator_setValue_chipClassesGenerated() {
        click("set-generator");
        click("set-value");

        assertChipClassNames("item-foo", "item-bar");
    }

    @Test
    public void setValue_setClassNameGenerator_chipClassesGenerated() {
        click("set-value");
        click("set-generator");

        assertChipClassNames("item-foo", "item-bar");
    }

    @Test
    public void changeClassNameGeneratorToReturnNull_chipClassesRemoved() {
        click("set-generator");
        click("set-value");

        click("reset-generator");

        assertChipClassNames("", "");
    }

    private void assertItemClassNames(String... expectedClassNames) {
        TestBenchElement overlay = $("vaadin-multi-select-combo-box-overlay")
                .first();
        ElementQuery<TestBenchElement> items = overlay
                .$("vaadin-multi-select-combo-box-item");

        for (int i = 0; i < expectedClassNames.length; i++) {
            Assert.assertEquals(items.get(i).getAttribute("class"),
                    expectedClassNames[i]);
        }
    }

    private void assertChipClassNames(String... expectedClassNames) {
        ElementQuery<TestBenchElement> chips = comboBox
                .$("vaadin-multi-select-combo-box-chip");

        for (int i = 0; i < expectedClassNames.length; i++) {
            // Skip first chip as it's used for overflow items
            Assert.assertEquals(chips.get(i + 1).getAttribute("class"),
                    expectedClassNames[i]);
        }
    }

    private void click(String id) {
        $(TestBenchElement.class).id(id).click();
    }
}
