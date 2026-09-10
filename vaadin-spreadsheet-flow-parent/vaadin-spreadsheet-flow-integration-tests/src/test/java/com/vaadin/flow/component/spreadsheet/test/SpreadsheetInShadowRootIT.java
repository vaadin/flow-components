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

        // The overlay is drawn inside the spreadsheet's own shadow root,
        // itself inside the host's — two boundaries down from the
        // document, so no document query reaches it.
        waitUntil(driver -> Boolean.TRUE.equals(executeScript("""
                const sheet = document.getElementById('shadow-host')
                    .shadowRoot.querySelector('vaadin-spreadsheet');
                const overlay = sheet && sheet.shadowRoot
                    && sheet.shadowRoot.querySelector('.popup-overlay');
                return !!overlay && !overlay.hidden;
                """)));

        String border = (String) executeScript(
                """
                        const sheet = document.getElementById('shadow-host')
                            .shadowRoot.querySelector('vaadin-spreadsheet');
                        const overlay = sheet.shadowRoot.querySelector('.popup-overlay');
                        return getComputedStyle(overlay).borderTopStyle;
                        """);

        // The component styles its own overlay from inside its shadow
        // root, so nesting cannot cut them off. An unstyled div reports
        // "none" here.
        Assert.assertEquals(
                "Overlay styles should reach the overlay even when the "
                        + "spreadsheet is nested in a shadow root",
                "solid", border);
    }
}
