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
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.TestPath;

@TestPath("spreadsheet-filter")
public class SpreadsheetFilterIT extends AbstractSpreadsheetIT {

    private static final String SELECT_ALL = "(Select All)";

    @Before
    public void init() {
        open();
        getSpreadsheet();
    }

    @Test
    public void filterColumn_otherColumnOmitsValuesOfHiddenRows() {
        // Before filtering, Column C offers all of its values
        openFilterPopup(3);
        Assert.assertEquals(List.of("Alice", "Bob", "Carol"),
                getFilterOptions("Column C"));
        closeFilterPopup("Column C");

        // Filter Column A so that the "Alpha" row gets hidden
        openFilterPopup(1);
        uncheckFilterOption("Column A", "Alpha");
        closeFilterPopup("Column A");

        // Column C no longer offers "Alice", as its row is hidden by Column A
        openFilterPopup(3);
        Assert.assertEquals(List.of("Bob", "Carol"),
                getFilterOptions("Column C"));
    }

    /**
     * Opens the filter pop-up of the given one-based column (Column A == 1).
     */
    private void openFilterPopup(int column) {
        findElementInShadowRoot(
                By.cssSelector(".col" + column + ".row1 .popupbutton")).click();
    }

    /**
     * Returns the filter pop-up overlay whose header matches the given caption.
     */
    private WebElement getFilterOverlay(String caption) {
        return waitUntil(driver -> driver
                .findElements(
                        By.cssSelector(".v-spreadsheet-popupbutton-overlay"))
                .stream()
                .filter(overlay -> caption.equals(
                        overlay.findElement(By.cssSelector(".header-caption"))
                                .getText()))
                .findFirst().orElse(null));
    }

    /**
     * Returns the value options listed in the given column's filter pop-up,
     * excluding the "(Select All)" entry.
     */
    private List<String> getFilterOptions(String caption) {
        return getFilterOverlay(caption)
                .findElements(By.tagName("vaadin-checkbox")).stream()
                .map(checkbox -> checkbox.getText().trim())
                .filter(text -> !text.isEmpty() && !SELECT_ALL.equals(text))
                .collect(Collectors.toList());
    }

    /**
     * Unchecks the given value option in the given column's filter pop-up,
     * which hides the matching rows.
     */
    private void uncheckFilterOption(String caption, String option) {
        WebElement checkbox = getFilterOverlay(caption)
                .findElements(By.tagName("vaadin-checkbox")).stream()
                .filter(c -> option.equals(c.getText().trim())).findFirst()
                .orElseThrow();
        checkbox.click();
        getCommandExecutor().waitForVaadin();
    }

    /**
     * Closes the given column's filter pop-up and waits until it is gone.
     */
    private void closeFilterPopup(String caption) {
        getFilterOverlay(caption)
                .findElement(By.cssSelector(".v-window-closebox")).click();
        waitUntil(driver -> driver
                .findElements(
                        By.cssSelector(".v-spreadsheet-popupbutton-overlay"))
                .stream().noneMatch(overlay -> {
                    var headers = overlay
                            .findElements(By.cssSelector(".header-caption"));
                    return !headers.isEmpty()
                            && caption.equals(headers.get(0).getText());
                }));
    }
}
