package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

@TestPath("vaadin-multi-select-combo-box/server-side-filtering")
public class MultiSelectComboBoxServerSideFilteringIT
        extends AbstractComponentIT {
    private MultiSelectComboBoxElement comboBox;

    @Before
    public void init() {
        open();
        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();
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
        Assert.assertEquals(2, options.size());
        Assert.assertTrue("Should display Item 10",
                options.contains("Item 10"));
        Assert.assertTrue("Should display Item 100",
                options.contains("Item 100"));
    }

    @Test
    public void setNonMatchingFilter_noItems() {
        comboBox.setFilter("Item XYZ");

        List<String> options = comboBox.getOptions();
        Assert.assertEquals(0, options.size());
    }
}
