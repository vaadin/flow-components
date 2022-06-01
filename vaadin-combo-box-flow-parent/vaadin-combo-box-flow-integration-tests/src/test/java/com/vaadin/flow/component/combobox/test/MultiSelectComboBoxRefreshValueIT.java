package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Set;

@TestPath("vaadin-multi-select-combo-box/refresh-value")
public class MultiSelectComboBoxRefreshValueIT extends AbstractComponentIT {
    private MultiSelectComboBoxElement comboBox;
    private TestBenchElement changeItemLabelGenerator;
    private TestBenchElement changeItemData;

    @Before
    public void init() {
        open();
        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();
        changeItemLabelGenerator = $("button")
                .id("change-item-label-generator");
        changeItemData = $("button").id("change-item-data");
    }

    @Test
    public void selectItems_changeItemLabelGenerator_valueUpdated() {
        comboBox.selectByText("Item 1");
        comboBox.selectByText("Item 10");
        changeItemLabelGenerator.click();

        assertSelectedItems(Set.of("Custom Item 1", "Custom Item 10"));
    }

    @Test
    @Ignore("https://github.com/vaadin/flow-components/issues/3239")
    public void selectItems_changeItemData_valueUpdated() {
        comboBox.selectByText("Item 1");
        comboBox.selectByText("Item 10");
        changeItemData.click();

        assertSelectedItems(Set.of("Updated Item 1", "Updated Item 10"));
    }

    private void assertSelectedItems(Set<String> items) {
        List<String> selectedTexts = comboBox.getSelectedTexts();
        Assert.assertEquals("Number of selected items does not match",
                items.size(), selectedTexts.size());
        items.forEach(item -> Assert.assertTrue(
                "Selection does not include item: " + item,
                selectedTexts.contains(item)));
    }
}
