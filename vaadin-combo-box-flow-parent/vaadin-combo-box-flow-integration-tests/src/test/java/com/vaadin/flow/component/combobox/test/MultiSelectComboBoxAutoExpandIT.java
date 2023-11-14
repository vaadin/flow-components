package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-multi-select-combo-box/auto-expand")
public class MultiSelectComboBoxAutoExpandIT extends AbstractComponentIT {
    private MultiSelectComboBoxElement comboBox;
    private TestBenchElement expandHorizontal;
    private TestBenchElement expandVertical;
    private TestBenchElement expandBoth;
    private TestBenchElement expandNone;

    @Before
    public void init() {
        open();
        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();
        expandHorizontal = $("button").id("expand-horizontal");
        expandVertical = $("button").id("expand-vertical");
        expandBoth = $("button").id("expand-both");
        expandNone = $("button").id("expand-none");
    }

    @Test
    public void expandHorizontal_selectItems_allChipsVisible() {
        expandHorizontal.click();

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
    public void selectItems_expandHorizontal_allChipsVisible() {
        comboBox.selectByText("Item 1");
        comboBox.selectByText("Item 2");
        comboBox.selectByText("Item 3");

        expandHorizontal.click();

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
    public void expandVertical_selectItems_allChipsVisible() {
        expandVertical.click();

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
    public void expandBoth_selectItems_allChipsVisible() {
        expandBoth.click();

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
    public void selectItems_expandNone_chipsCollapsed() {
        expandHorizontal.click();

        comboBox.selectByText("Item 10");
        comboBox.selectByText("Item 20");
        comboBox.selectByText("Item 30");

        expandNone.click();

        ElementQuery<TestBenchElement> chips = comboBox
                .$("vaadin-multi-select-combo-box-chip");

        Assert.assertEquals("Overflow chip + lasts selected shown", 2,
                chips.all().size());
    }
}
