/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet")
public class ZZPopupShiftProbeIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
    }

    private String state(String tag) {
        return "PROBE " + tag + " " + executeScript("""
                const host = arguments[0];
                const g = host.shadowRoot.querySelector(
                    'vaadin-spreadsheet-internal-cell-grid');
                const rendered = [...g.shadowRoot.querySelectorAll(
                    '.popup-button, .popupbutton')]
                    .map(b => b.dataset.col + ':' + b.dataset.row).join(',');
                return 'prop=' + JSON.stringify(host.popupButtons)
                    + ' rendered=[' + rendered + ']';
                """, getSpreadsheet());
    }

    @Test
    public void probeDelete() {
        clickCell("B1");
        loadTestFixture(TestFixtures.PopupButton);
        clickCell("A2");
        System.out.println(state("before-delete"));
        clickCell("A1");
        loadTestFixture(TestFixtures.DeleteRow);
        getCommandExecutor().waitForVaadin();
        System.out.println(state("after-delete-row1"));
    }
}
