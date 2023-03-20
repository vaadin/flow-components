
package com.vaadin.flow.component.listbox.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.listbox.testbench.ListBoxElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-list-box/multi-select")
public class MultiSelectListBoxIT extends AbstractComponentIT {

    private ListBoxElement listBox;

    @Before
    public void init() {
        open();
        checkLogsForErrors();
        listBox = $(ListBoxElement.class).first();
    }

    @Test
    public void clickItems_checkValueChangeEvents() {
        clickItem(1);
        assertValueChanges(true, "bar");
        clickItem(0);
        assertValueChanges(true, "bar", "bar, foo");
        clickItem(1);
        assertValueChanges(true, "bar", "bar, foo", "foo");
        clickItem(0);
        assertValueChanges(true, "bar", "bar, foo", "foo", "");
    }

    @Test
    public void setValueFromServer_checkValueChangeEvents() {
        findElement(By.id("setValue")).click();
        assertValueChanges(false, "bar, qux");
    }

    @Test
    public void setValueFromServer_checkSelectedItemsProperty() {
        findElement(By.id("setValue")).click();
        assertSelectedValues("1,3");
    }

    @Test
    public void setValueFromServer_clickItems_checkValueChangeEvents() {
        findElement(By.id("setValue")).click();
        clickItem(1);
        assertValueChanges(true, "bar, qux", "qux");
    }

    @Test
    public void setValueFromServer_clickItems_checkSelectedItemsProperty() {
        findElement(By.id("setValue")).click();
        clickItem(1);
        assertSelectedValues("3");
    }

    private void assertValueChanges(boolean fromClient, String... expected) {
        assertLastEventFromClient(fromClient);
        String[] values = $("div").id("valueChanges").$("p").all().stream()
                .map(TestBenchElement::getText).toArray(String[]::new);
        Assert.assertArrayEquals(expected, values);
    }

    private void assertLastEventFromClient(boolean expected) {
        Assert.assertEquals(
                "Unexpected value of isClient flag of the ValueChangeEvent",
                String.valueOf(expected), $("span").id("fromClient").getText());
    }

    private void assertSelectedValues(String expected) {
        String selectedValues = (String) executeScript(
                "return arguments[0].selectedValues.toString()", listBox);
        Assert.assertEquals("Unexpected selectedValues property", expected,
                selectedValues);
    }

    private void clickItem(int index) {
        listBox.$("vaadin-item").all().get(index).click();
    }

}
