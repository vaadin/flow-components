/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-combo-box/detach-reattach")
public class DetachReattachIT extends AbstractComboBoxIT {

    private ComboBoxElement combo;

    @Before
    public void init() {
        open();
        combo = $(ComboBoxElement.class).first();
    }

    @Test
    public void detachComboBox_reattach_noClientErrors() {
        clickButton("detach");
        clickButton("attach");
        checkLogsForErrors();
    }

    @Test
    public void detachComboBox_reattachRedetach_noClientErrors() {
        clickButton("detach");
        clickButton("attach-detach");
        checkLogsForErrors();
    }

    @Test
    public void openComboBox_detach_reattach_open_itemsLoaded() {
        combo.openPopup();
        assertRendered("foo");
        clickButton("detach");
        clickButton("attach");
        combo = $(ComboBoxElement.class).first();
        combo.openPopup();
        assertLoadedItemsCount("Expected 2 items to be loaded", 2, combo);
    }

    @Test
    public void setValueFromServer_selectOtherValue_detach_reattach_valueNotChanged() {
        clickButton("set-value");
        assertValueChanges("foo");

        combo.openPopup();
        combo.selectByText("bar");
        assertValueChanges("foo", "bar");

        clickButton("detach");
        clickButton("attach");
        assertValueChanges("foo", "bar");
    }

    private void assertValueChanges(String... expected) {
        String[] valueChanges = $("div").id("value-changes").$("p").all()
                .stream().map(TestBenchElement::getText).toArray(String[]::new);
        Assert.assertArrayEquals(expected, valueChanges);
    }

}
