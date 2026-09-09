/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("spreadsheet-in-shadow-root")
public class SpreadsheetInShadowRootIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void spreadsheetNestedInShadowRoot_overlayIsStyled() {
        SpreadsheetElement spreadsheet = $(DivElement.class).id("shadow-host")
                .$(SpreadsheetElement.class).first();
        // Selecting a cell opens a popup-button overlay (see the page).
        spreadsheet.getCellAt(2, 2).click();

        // The overlay lives inside the host's shadow root, so it can't be
        // located at document level; drill through the shadow root instead.
        waitUntil(driver -> (Boolean) executeScript(
                "return !!document.getElementById('shadow-host').shadowRoot"
                        + ".querySelector('.v-spreadsheet-popupbutton-overlay');"));

        String borderRadius = (String) executeScript(
                "return getComputedStyle(document.getElementById('shadow-host')"
                        + ".shadowRoot.querySelector('.v-spreadsheet-popupbutton-overlay'))"
                        + ".borderRadius;");

        // Unstyled (bug): the UA popover default border-radius is 0px. Styled
        // (fixed): the overlay stylesheet, adopted onto the shadow root, sets a
        // non-zero border-radius.
        Assert.assertNotEquals(
                "Overlay styles should reach the overlay even when the "
                        + "spreadsheet is nested in a shadow root",
                "0px", borderRadius);
    }
}
