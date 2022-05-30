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

@TestPath("vaadin-multi-select-combo-box/value-change")
public class MultiSelectComboBoxValueChangeIT
        extends AbstractComponentIT {
    private MultiSelectComboBoxElement comboBox;
    private TestBenchElement setServerSideValue;
    private TestBenchElement eventValue;
    private TestBenchElement eventOrigin;

    @Before
    public void init() {
        open();
        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();
        setServerSideValue = $("button").id("set-server-side-value");
        eventValue = $("span").id("event-value");
        eventOrigin = $("span").id("event-origin");
    }

    @Test
    public void inputValue_valueUpdated() {
        comboBox.selectByText("Item 1");
        comboBox.selectByText("Item 10");

        assertVisibleChips(Set.of("Item 1", "Item 10"));
        assertValueChange(Set.of("Item 1", "Item 10"), "client");
    }

    @Test
    public void inputValue_removeValue_valueUpdated() {
        comboBox.selectByText("Item 1");
        // Popup needs to be closed in order to remove chips with backspace
        comboBox.closePopup();
        // Select the chip
        comboBox.sendKeys(Keys.BACK_SPACE);
        // Delete the chip
        comboBox.sendKeys(Keys.BACK_SPACE);

        assertVisibleChips(Collections.emptySet());
        assertValueChange(Collections.emptySet(), "client");
    }

    @Test
    public void selectItemsFromPopup_valueUpdated() {
        comboBox.openPopup();
        comboBox.waitForLoadingFinished();
        // Select "Item 1"
        comboBox.sendKeys(Keys.DOWN, Keys.ENTER);

        comboBox.openPopup();
        comboBox.waitForLoadingFinished();
        // Select "Item 3", starting from preselected "Item 1"
        comboBox.sendKeys(Keys.DOWN, Keys.DOWN, Keys.ENTER);

        assertVisibleChips(Set.of("Item 1", "Item 3"));
        assertValueChange(Set.of("Item 1", "Item 3"), "client");
    }

    @Test
    public void setServerSideValue_valueUpdated() {
        setServerSideValue.click();

        assertVisibleChips(Set.of("Item 1", "Item 2"));
        assertValueChange(Set.of("Item 1", "Item 2"), "server");
    }

    private void assertVisibleChips(Set<String> items) {
        List<String> chips = comboBox.getVisibleChips();
        Assert.assertEquals("Number of selected items does not match",
                items.size(), chips.size());
        items.forEach(item -> Assert.assertTrue(
                "Chips do not include item: " + item, chips.contains(item)));
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
