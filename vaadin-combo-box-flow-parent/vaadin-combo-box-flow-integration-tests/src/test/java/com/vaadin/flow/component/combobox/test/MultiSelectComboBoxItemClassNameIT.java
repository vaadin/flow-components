/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

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
            Assert.assertEquals(items.get(i).getDomAttribute("class"),
                    expectedClassNames[i]);
        }
    }

    private void assertChipClassNames(String... expectedClassNames) {
        ElementQuery<TestBenchElement> chips = comboBox
                .$("vaadin-multi-select-combo-box-chip");

        for (int i = 0; i < expectedClassNames.length; i++) {
            // Skip first chip as it's used for overflow items
            Assert.assertEquals(chips.get(i + 1).getDomAttribute("class"),
                    expectedClassNames[i]);
        }
    }

    private void click(String id) {
        $(TestBenchElement.class).id(id).click();
    }
}
