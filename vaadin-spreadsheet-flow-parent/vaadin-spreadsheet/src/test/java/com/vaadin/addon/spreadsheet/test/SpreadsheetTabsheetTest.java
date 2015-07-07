package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.addon.spreadsheet.test.demoapps.TabsheetTestUI;
import com.vaadin.testbench.elements.TabSheetElement;

public class SpreadsheetTabsheetTest extends AbstractSpreadsheetTestCase {

    @Override
    protected Class<?> getUIClass() {
        return TabsheetTestUI.class;
    }

    @Test
    public void spreadsheetWithTableInTabsheet_changeTabs_tableVisible()
            throws Exception {
        TabSheetElement tabSheet = $(TabSheetElement.class).get(0);
        // first tab open by default no spreadsheet in this tab
        checkPopupButtons(0);
        // open second tab
        tabSheet.openTab(1);
        // tab contains a spreadsheet with a 4x4 table
        checkPopupButtons(4);
        // open third tab
        tabSheet.openTab(2);
        // no spreadsheet in this tab
        checkPopupButtons(0);
        // re-open second tab
        tabSheet.openTab(1);
        // tab contains a spreadsheet with a 4x4 table
        checkPopupButtons(4);
    }

    public void checkPopupButtons(int expected) {
        int actual = getDriver().findElements(By.className("popupbutton"))
                .size();
        assertTrue(expected + " PopupButtons were expected, but " + actual
                + " were found.", actual == expected);
    }

}
