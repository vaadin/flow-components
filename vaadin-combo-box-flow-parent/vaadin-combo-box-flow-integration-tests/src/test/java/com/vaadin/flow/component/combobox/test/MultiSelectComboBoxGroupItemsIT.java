package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-multi-select-combo-box/group-items")
public class MultiSelectComboBoxGroupItemsIT extends AbstractComponentIT {
    private MultiSelectComboBoxElement comboBox;
    private TestBenchElement groupSelected;
    private TestBenchElement ungroupSelected;

    @Before
    public void init() {
        open();
        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();
        groupSelected = $("button").id("group-selected");
        ungroupSelected = $("button").id("ungroup-selected");
    }

    @Test
    public void groupSelectedItems_selectItems_itemsGrouped() {
        groupSelected.click();

        comboBox.selectByText("Item 2");
        comboBox.selectByText("Item 3");

        comboBox.openPopup();
        comboBox.waitForLoadingFinished();

        TestBenchElement overlay = $("vaadin-multi-select-combo-box-overlay")
                .first();
        ElementQuery<TestBenchElement> items = overlay
                .$("vaadin-multi-select-combo-box-item");

        TestBenchElement item1 = items.get(0);
        TestBenchElement item2 = items.get(1);

        Assert.assertEquals("Item 2", item1.getText());
        Assert.assertEquals("Item 3", item2.getText());

        Assert.assertTrue(item1.hasAttribute("selected"));
        Assert.assertTrue(item2.hasAttribute("selected"));
    }

    @Test
    public void selectItems_groupSelectedItems_itemsGrouped() {
        comboBox.selectByText("Item 2");
        comboBox.selectByText("Item 3");

        groupSelected.click();

        comboBox.openPopup();
        comboBox.waitForLoadingFinished();

        TestBenchElement overlay = $("vaadin-multi-select-combo-box-overlay")
                .first();
        ElementQuery<TestBenchElement> items = overlay
                .$("vaadin-multi-select-combo-box-item");

        TestBenchElement item1 = items.get(0);
        TestBenchElement item2 = items.get(1);

        Assert.assertEquals("Item 2", item1.getText());
        Assert.assertEquals("Item 3", item2.getText());

        Assert.assertTrue(item1.hasAttribute("selected"));
        Assert.assertTrue(item2.hasAttribute("selected"));
    }

    @Test
    public void groupSelectedItems_ungroupItems_itemsNotGrouped() {
        groupSelected.click();

        comboBox.selectByText("Item 2");
        comboBox.selectByText("Item 3");

        comboBox.openPopup();
        comboBox.waitForLoadingFinished();

        comboBox.closePopup();

        ungroupSelected.click();

        comboBox.openPopup();
        comboBox.waitForLoadingFinished();

        TestBenchElement overlay = $("vaadin-multi-select-combo-box-overlay")
                .first();
        ElementQuery<TestBenchElement> items = overlay
                .$("vaadin-multi-select-combo-box-item");

        TestBenchElement item1 = items.get(0);
        TestBenchElement item2 = items.get(1);

        Assert.assertEquals("Item 1", item1.getText());
        Assert.assertEquals("Item 2", item2.getText());

        Assert.assertFalse(item1.hasAttribute("selected"));
        Assert.assertTrue(item2.hasAttribute("selected"));
    }
}
