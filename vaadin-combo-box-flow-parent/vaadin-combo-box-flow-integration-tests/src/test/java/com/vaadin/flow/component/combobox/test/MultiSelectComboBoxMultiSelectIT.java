package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@TestPath("vaadin-multi-select-combo-box/multi-select")
public class MultiSelectComboBoxMultiSelectIT extends AbstractComponentIT {
    private MultiSelectComboBoxElement comboBox;
    private TestBenchElement selectItems;
    private TestBenchElement deselectItems;
    private TestBenchElement deselectAll;
    private TestBenchElement eventValue;
    private TestBenchElement eventOrigin;

    @Before
    public void init() {
        open();
        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();
        selectItems = $("button").id("select-items");
        deselectItems = $("button").id("deselect-items");
        deselectAll = $("button").id("deselect-all");
        eventValue = $("span").id("event-value");
        eventOrigin = $("span").id("event-origin");
    }

    @Test
    public void selectItemsServerSide_selectedItemsUpdated() {
        selectItems.click();

        assertSelectedItems(Set.of("Item 1", "Item 2", "Item 3"));
        assertSelectionChange(Set.of("Item 1", "Item 2", "Item 3"), "server");
    }

    @Test
    public void selectItemsServerSide_deselectItemsServerSide_selectedItemsUpdated() {
        selectItems.click();
        deselectItems.click();

        assertSelectedItems(Set.of("Item 3"));
        assertSelectionChange(Set.of("Item 3"), "server");
    }

    @Test
    public void selectItemsServerSide_deselectAllServerSide_selectedItemsUpdated() {
        selectItems.click();
        deselectAll.click();

        assertSelectedItems(Collections.emptySet());
        assertSelectionChange(Collections.emptySet(), "server");
    }

    @Test
    public void selectItemsClientSide_triggersSelectionChange() {
        comboBox.selectByText("Item 1");
        assertSelectionChange(Set.of("Item 1"), "client");

        comboBox.selectByText("Item 2");
        assertSelectionChange(Set.of("Item 1", "Item 2"), "client");

        comboBox.selectByText("Item 3");
        assertSelectionChange(Set.of("Item 1", "Item 2", "Item 3"), "client");
    }

    @Test
    public void selectItemsClientSide_deselectItemsServerSide_selectedItemsUpdated() {
        comboBox.selectByText("Item 1");
        comboBox.selectByText("Item 2");
        comboBox.selectByText("Item 3");
        deselectItems.click();

        assertSelectedItems(Set.of("Item 3"));
        assertSelectionChange(Set.of("Item 3"), "server");
    }

    @Test
    public void selectItemsServerSide_deselectItemsClientSide_selectedItemsUpdated() {
        selectItems.click();
        comboBox.deselectByText("Item 1");
        comboBox.deselectByText("Item 2");

        assertSelectedItems(Set.of("Item 3"));
        assertSelectionChange(Set.of("Item 3"), "client");
    }

    private void assertSelectedItems(Set<String> items) {
        List<String> selectedTexts = comboBox.getSelectedTexts();
        Assert.assertEquals("Number of selected items does not match",
                items.size(), selectedTexts.size());
        items.forEach(item -> Assert.assertTrue(
                "Selection does not include item: " + item,
                selectedTexts.contains(item)));
    }

    private void assertSelectionChange(Set<String> items, String origin) {
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
