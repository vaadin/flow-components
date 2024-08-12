/**
 * Copyright 2000-2024 Vaadin Ltd.
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
import org.openqa.selenium.By;

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.component.tabs.testbench.TabSheetElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet/tabsheet")
public class SpreadsheetTabSheetIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void spreadsheetWithTableInTabsheet_changeTabs_tableVisible()
            throws Exception {
        TabSheetElement tabSheet = $(TabSheetElement.class).get(0);
        // first tab open by default no spreadsheet in this tab
        checkPopupButtons(0);
        // open second tab
        tabSheet.setSelectedTabIndex(1);
        // tab contains a spreadsheet with a 4x4 table
        checkPopupButtons(4);
        // open third tab
        tabSheet.setSelectedTabIndex(2);
        // no spreadsheet in this tab
        checkPopupButtons(0);
        // re-open second tab
        tabSheet.setSelectedTabIndex(1);
        // tab contains a spreadsheet with a 4x4 table
        checkPopupButtons(4);
    }

    public void checkPopupButtons(int expected) {
        var spreadsheet = $(SpreadsheetElement.class);
        int actual = 0;
        if (spreadsheet.exists()) {
            actual = findElementsInShadowRoot(By.className("popupbutton"))
                    .size();
        }
        Assert.assertTrue(expected + " PopupButtons were expected, but "
                + actual + " were found.", actual == expected);
    }

}
