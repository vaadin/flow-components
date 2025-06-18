/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-multi-select-combo-box/value-change")
public class MultiSelectComboBoxValueChangeIT extends AbstractComponentIT {
    private MultiSelectComboBoxElement comboBox;
    private TestBenchElement setServerSideValue;
    private TestBenchElement clearServerSideValue;
    private TestBenchElement eventValue;
    private TestBenchElement eventOrigin;

    @Before
    public void init() {
        open();
        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();
        setServerSideValue = $("button").id("set-server-side-value");
        clearServerSideValue = $("button").id("clear-server-side-value");
        eventValue = $("span").id("event-value");
        eventOrigin = $("span").id("event-origin");
    }

    @Test
    public void selectItem_valueUpdated() {
        comboBox.selectByText("Item 1");
        comboBox.selectByText("Item 10");

        assertSelectedItems(Set.of("Item 1", "Item 10"));
        assertValueChange(Set.of("Item 1", "Item 10"), "client");
    }

    @Test
    public void selectItem_deselectItem_valueUpdated() {
        comboBox.selectByText("Item 1");
        assertSelectedItems(Set.of("Item 1"));
        assertValueChange(Set.of("Item 1"), "client");

        comboBox.deselectByText("Item 1");
        assertSelectedItems(Collections.emptySet());
        assertValueChange(Collections.emptySet(), "client");
    }

    @Test
    public void selectItemsWithKeyboardNavigation_valueUpdated() {
        comboBox.openPopup();
        comboBox.waitForLoadingFinished();
        // Select "Item 1"
        comboBox.sendKeys(Keys.DOWN, Keys.ENTER);

        comboBox.openPopup();
        comboBox.waitForLoadingFinished();
        // Select "Item 3", starting from preselected "Item 1"
        comboBox.sendKeys(Keys.DOWN, Keys.DOWN, Keys.ENTER);

        assertSelectedItems(Set.of("Item 1", "Item 3"));
        assertValueChange(Set.of("Item 1", "Item 3"), "client");
    }

    @Test
    public void setServerSideValue_valueUpdated() {
        setServerSideValue.click();

        assertSelectedItems(Set.of("Item 1", "Item 2"));
        assertValueChange(Set.of("Item 1", "Item 2"), "server");
    }

    @Test
    public void selectItems_clearValueServerSide_valueCleared() {
        comboBox.selectByText("Item 1");
        comboBox.selectByText("Item 10");
        clearServerSideValue.click();

        assertSelectedItems(Collections.emptySet());
        assertValueChange(Collections.emptySet(), "server");
    }

    private void assertSelectedItems(Set<String> items) {
        List<String> selectedTexts = comboBox.getSelectedTexts();
        Assert.assertEquals("Number of selected items does not match",
                items.size(), selectedTexts.size());
        items.forEach(item -> Assert.assertTrue(
                "Selection does not include item: " + item,
                selectedTexts.contains(item)));
    }

    private void assertValueChange(Set<String> items, String origin) {
        String eventValuesText = eventValue.getText();
        List<String> eventValues = eventValuesText.isEmpty()
                ? Collections.emptyList()
                : Arrays.asList(eventValuesText.split(","));

        Assert.assertEquals("Number of selected items does not match",
                items.size(), eventValues.size());
        items.forEach(item -> Assert.assertTrue(
                "Event value does not include item: " + item,
                eventValues.contains(item)));
        Assert.assertEquals("Event should have originated from: " + origin,
                origin, eventOrigin.getText());
    }
}
