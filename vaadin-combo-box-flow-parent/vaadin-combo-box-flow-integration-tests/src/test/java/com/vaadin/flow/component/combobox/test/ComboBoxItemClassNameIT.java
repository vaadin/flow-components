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

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-combo-box/item-class-name")
public class ComboBoxItemClassNameIT extends AbstractComponentIT {

    private ComboBoxElement comboBox;

    @Before
    public void init() {
        open();
        comboBox = $(ComboBoxElement.class).first();
    }

    @Test
    public void noClassesOnItemsSetInitially() {
        comboBox.openPopup();

        assertItemClassNames("", "", "");
    }

    @Test
    public void setClassNameGenerator_classesGenerated() {
        click("set-generator");
        comboBox.openPopup();

        assertItemClassNames("item-foo", "item-bar", "item-baz");
    }

    @Test
    public void changeClassNameGeneratorToReturnNull_classesRemoved() {
        click("set-generator");
        click("reset-generator");
        comboBox.openPopup();

        assertItemClassNames("", "", "");
    }

    private void assertItemClassNames(String... expectedClassNames) {
        TestBenchElement overlay = $("vaadin-combo-box-overlay").first();
        ElementQuery<TestBenchElement> items = overlay
                .$("vaadin-combo-box-item");

        for (int i = 0; i < expectedClassNames.length; i++) {
            Assert.assertEquals(items.get(i).getDomAttribute("class"),
                    expectedClassNames[i]);
        }
    }

    private void click(String id) {
        $(TestBenchElement.class).id(id).click();
    }
}
