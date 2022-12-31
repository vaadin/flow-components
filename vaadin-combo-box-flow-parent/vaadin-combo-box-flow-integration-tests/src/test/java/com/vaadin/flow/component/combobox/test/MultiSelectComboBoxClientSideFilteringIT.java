package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

@TestPath("vaadin-multi-select-combo-box/client-side-filtering")
public class MultiSelectComboBoxClientSideFilteringIT
        extends AbstractComponentIT {
    private MultiSelectComboBoxElement comboBox;

    @Before
    public void init() {
        open();
        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();
    }

    @Test
    public void openPopup_showsAllItems() {
        comboBox.openPopup();
        comboBox.waitForLoadingFinished();

        List<String> options = comboBox.getOptions();
        Assert.assertEquals(10, options.size());
    }

    @Test
    public void setFilter_noErrors() {
        comboBox.setFilter("Item 10");
        checkLogsForErrors();
    }

    @Test
    public void setFilter_closePopup_noErrors() {
        comboBox.setFilter("Item 10");
        comboBox.closePopup();
        checkLogsForErrors();
    }

    @Test
    public void setMatchingFilter_filtersItems() {
        comboBox.setFilter("Item 10");

        List<String> options = comboBox.getOptions();
        Assert.assertEquals(1, options.size());
        Assert.assertTrue("Should display Item 10",
                options.contains("Item 10"));
    }

    @Test
    public void inputMatchingFilter_filtersItems() {
        comboBox.openPopup();
        comboBox.sendKeys("Item 10");

        List<String> options = comboBox.getOptions();
        Assert.assertEquals(1, options.size());
        Assert.assertTrue("Should display Item 10",
                options.contains("Item 10"));
    }

    @Test
    public void setNonMatchingFilter_noItems() {
        comboBox.setFilter("Item XYZ");

        List<String> options = comboBox.getOptions();
        Assert.assertEquals(0, options.size());
    }

    @Test
    public void setFilter_clearFilter_showsAllItems() {
        comboBox.setFilter("Item 10");
        comboBox.setFilter("");

        List<String> options = comboBox.getOptions();
        Assert.assertEquals(10, options.size());
    }
}
