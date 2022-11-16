package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

@TestPath("vaadin-multi-select-combo-box/detach-reattach")
public class MultiSelectComboBoxDetachReattachIT extends AbstractComponentIT {
    private MultiSelectComboBoxElement comboBox;
    private TestBenchElement detach;
    private TestBenchElement attach;

    @Before
    public void init() {
        open();
        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();
        detach = $(TestBenchElement.class).id("detach");
        attach = $(TestBenchElement.class).id("attach");
    }

    @Test
    public void selectFromClient_detach_reattach_hasSelectedItems() {
        comboBox.selectByText("Item 1");
        comboBox.selectByText("Item 2");
        comboBox.selectByText("Item 3");
        detach.click();
        attach.click();

        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();
        List<String> expectedChips = List.of("Item 1", "Item 2", "Item 3");
        Assert.assertEquals(expectedChips, comboBox.getSelectedTexts());
    }
}
