package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-multi-select-combo-box/all-chips")
public class MultiSelectComboBoxAllChipsIT extends AbstractComponentIT {
    private MultiSelectComboBoxElement comboBox;
    private TestBenchElement showAllChips;
    private TestBenchElement dontShowAllChips;

    @Before
    public void init() {
        open();
        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();
        showAllChips = $("button").id("show-all-chips");
        dontShowAllChips = $("button").id("dont-show-all-chips");
    }

    @Test
    public void showAllChips_selectItems_allChipsVisible() {
        showAllChips.click();

        comboBox.selectByText("Item 1");
        comboBox.selectByText("Item 2");
        comboBox.selectByText("Item 3");

        ElementQuery<TestBenchElement> chips = comboBox
                .$("vaadin-multi-select-combo-box-chip");

        Assert.assertEquals("All chips plus overflow chips", 4,
                chips.all().size());

        TestBenchElement chip1 = chips.get(1);
        TestBenchElement chip2 = chips.get(2);

        Assert.assertEquals("Item 1", chip1.getText());
        Assert.assertEquals("Item 2", chip2.getText());
    }

    @Test
    public void selectItems_showAllChips_allChipsVisible() {
        comboBox.selectByText("Item 1");
        comboBox.selectByText("Item 2");
        comboBox.selectByText("Item 3");

        showAllChips.click();

        ElementQuery<TestBenchElement> chips = comboBox
                .$("vaadin-multi-select-combo-box-chip");

        Assert.assertEquals("All chips plus overflow chips", 4,
                chips.all().size());

        TestBenchElement chip1 = chips.get(1);
        TestBenchElement chip2 = chips.get(2);
        TestBenchElement chip3 = chips.get(3);

        Assert.assertEquals("Item 1", chip1.getText());
        Assert.assertEquals("Item 2", chip2.getText());
        Assert.assertEquals("Item 3", chip3.getText());
    }

    @Test
    public void selectItems_dontShowAllChips_chipsCollapsed() {
        showAllChips.click();

        comboBox.selectByText("Item 10");
        comboBox.selectByText("Item 20");
        comboBox.selectByText("Item 30");

        dontShowAllChips.click();

        ElementQuery<TestBenchElement> chips = comboBox
                .$("vaadin-multi-select-combo-box-chip");

        Assert.assertEquals("Only overflow chip is shown", 1,
                chips.all().size());
    }
}
