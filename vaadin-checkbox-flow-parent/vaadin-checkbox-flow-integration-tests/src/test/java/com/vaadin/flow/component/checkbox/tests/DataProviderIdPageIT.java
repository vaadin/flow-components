
package com.vaadin.flow.component.checkbox.tests;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-checkbox/data-provider-id")
public class DataProviderIdPageIT extends AbstractComponentIT {

    @Test
    public void selectById_itemsAreNotEqualButHasSameId_itemIsSelected() {
        open();

        findElement(By.id("select-by-id")).click();

        TestBenchElement barCheckboxGroup = $("vaadin-checkbox-group")
                .id("id-data-provider").$("vaadin-checkbox").all().get(1);
        String isChecked = barCheckboxGroup.getAttribute("checked");
        Assert.assertEquals(Boolean.TRUE.toString(), isChecked);
    }

    @Test
    public void selectByEquals_itemsAreEqual_itemIsSelected() {
        open();

        findElement(By.id("select-by-equals")).click();

        TestBenchElement barCheckboxGroup = $("vaadin-checkbox-group")
                .id("standard-equals").$("vaadin-checkbox").all().get(1);
        String isChecked = barCheckboxGroup.getAttribute("checked");
        Assert.assertEquals(Boolean.TRUE.toString(), isChecked);
    }

    @Test
    public void selectById_itemsAreNotEqualButHasSameId_itemIsNotSelected() {
        open();

        findElement(By.id("no-selection")).click();

        TestBenchElement barCheckboxGroup = $("vaadin-checkbox-group")
                .id("id-data-provider").$("vaadin-checkbox").all().get(1);
        String isChecked = barCheckboxGroup.getAttribute("checked");
        Assert.assertNull(isChecked);
    }
}
