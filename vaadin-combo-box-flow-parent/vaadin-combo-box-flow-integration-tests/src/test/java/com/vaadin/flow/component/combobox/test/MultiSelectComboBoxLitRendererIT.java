/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
