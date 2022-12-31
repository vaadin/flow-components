package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.component.tabs.testbench.TabSheetElement;
import com.vaadin.flow.testutil.TestPath;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

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
