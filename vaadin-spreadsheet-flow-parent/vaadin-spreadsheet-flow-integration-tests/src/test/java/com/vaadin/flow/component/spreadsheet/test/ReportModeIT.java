package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet")
public class ReportModeIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
    }

    @Test
    public void toggleReportMode_noClientErrors() {
        WebElement reportModeButton = findElement(By.id("report-mode"));
        // Toggle report mode on
        reportModeButton.click();
        // Toggle report mode off
        reportModeButton.click();

        checkLogsForErrors(m -> !m.contains("IllegalArgumentException"));
    }

}
