
package com.vaadin.flow.component.checkbox.tests;

import java.util.List;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Test;

@TestPath("vaadin-checkbox-group-disabled-item")
public class CheckboxGroupDisabledItemIT extends AbstractComponentIT {

    @Test
    public void disabledGroupItemChecked() {
        open();
        TestBenchElement group = $(TestBenchElement.class)
                .id("checkbox-group-disabled-item");

        List<TestBenchElement> checkboxes = group.$("vaadin-checkbox").all();

        Assert.assertEquals(Boolean.TRUE.toString(),
                checkboxes.get(1).getAttribute("checked"));
    }

    @Test
    public void disabledItemCanBeCheckedProgrammatically() {
        open();
        TestBenchElement group = $(TestBenchElement.class)
                .id("checkbox-group-disabled-item");
        List<TestBenchElement> checkboxes = group.$("vaadin-checkbox").all();
        TestBenchElement secondCheckbox = checkboxes.get(1);
        TestBenchElement toggleBarButton = $("button").id("toggle-bar-button");

        // Deselect
        toggleBarButton.click();
        Assert.assertNull(secondCheckbox.getAttribute("checked"));

        // Reselect
        toggleBarButton.click();
        Assert.assertEquals(Boolean.TRUE.toString(),
                secondCheckbox.getAttribute("checked"));
    }

    /**
     * Regression test for:
     * https://github.com/vaadin/flow-components/issues/1185
     */
    @Test
    public void enabledItemCanBeCheckedManuallyWhenSettingItemEnabledProviderAfterSelectingValue() {
        open();
        TestBenchElement group = $(TestBenchElement.class)
                .id("checkbox-group-disabled-item");
        List<TestBenchElement> checkboxes = group.$("vaadin-checkbox").all();
        TestBenchElement firstCheckbox = checkboxes.get(0);

        // Select
        firstCheckbox.click();
        Assert.assertEquals(Boolean.TRUE.toString(),
                firstCheckbox.getAttribute("checked"));

        // Deselect
        firstCheckbox.click();
        Assert.assertNull(firstCheckbox.getAttribute("checked"));
    }

    @Test
    public void enablingTheGroupDoesnNotEnableItemDisabledWithItemEnabledProvider() {
        open();
        TestBenchElement group = $(TestBenchElement.class)
                .id("checkbox-group-disabled-item");
        List<TestBenchElement> checkboxes = group.$("vaadin-checkbox").all();
        TestBenchElement toggleEnabledButton = $("button")
                .id("toggle-enabled-button");

        // Disable group
        toggleEnabledButton.click();

        // Re-enable group
        toggleEnabledButton.click();

        Assert.assertEquals("Second checkbox should be disabled",
                Boolean.TRUE.toString(),
                checkboxes.get(1).getAttribute("disabled"));
    }

}
