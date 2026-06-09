/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.checkbox.testbench.CheckboxGroupElement;
import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("spreadsheet-filter")
public class SpreadsheetFilterIT extends AbstractComponentIT {

    private SpreadsheetElement spreadsheet;

    @Before
    public void init() {
        open();
        spreadsheet = $(SpreadsheetElement.class).single();
    }

    @Test
    public void filterColumn_otherColumnOmitsValuesOfHiddenRows() {
        // Before filtering, Column C offers all of its values
        spreadsheet.getCellAt("C1").popupButtonClick();
        Assert.assertEquals(List.of("Alice", "Bob", "Carol"),
                getFilterPopup().getOptions());
        closeFilterPopup();

        // Filter Column A so that the "Alpha" row gets hidden
        spreadsheet.getCellAt("A1").popupButtonClick();
        getFilterPopup().deselectByText("Alpha");
        closeFilterPopup();

        // Column C no longer offers "Alice", as its row is hidden by Column A
        spreadsheet.getCellAt("C1").popupButtonClick();
        Assert.assertEquals(List.of("Bob", "Carol"),
                getFilterPopup().getOptions());
    }

    @Test
    public void filterTwoColumns_eachColumnRetainsOwnFilteredValues() {
        // Filter Column A so that the "Alpha" row gets hidden
        spreadsheet.getCellAt("A1").popupButtonClick();
        getFilterPopup().deselectByText("Alpha");
        closeFilterPopup();

        // Filter Column B so that the "Bar" row gets hidden
        spreadsheet.getCellAt("B1").popupButtonClick();
        getFilterPopup().deselectByText("Bar");
        closeFilterPopup();

        // Column A still offers "Alpha" as an unchecked option, so its hidden
        // row can be brought back independently of Column B's filter
        spreadsheet.getCellAt("A1").popupButtonClick();
        Assert.assertEquals(List.of("Alpha", "Gamma"),
                getFilterPopup().getOptions());
        Assert.assertEquals(List.of("Gamma"),
                getFilterPopup().getSelectedTexts());
    }

    private CheckboxGroupElement getFilterPopup() {
        return $(CheckboxGroupElement.class).single();
    }

    private void closeFilterPopup() {
        findElement(By.className("v-window-closebox")).click();
        // The overlay fades out, so wait until it is actually gone before
        // opening the next one, otherwise single() would match two overlays.
        waitUntil(driver -> $(CheckboxGroupElement.class).all().isEmpty());
    }
}
