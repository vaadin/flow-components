
package com.vaadin.flow.component.datepicker;

import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-date-picker/date-picker-in-a-grid-header")
public class DatePickerInAGridHeaderIT extends AbstractComponentIT {

    /**
     * Test for https://github.com/vaadin/vaadin-date-picker-flow/issues/100
     */
    @Test
    public void openPage_datePickerIsRendereredWithoutErrors() {
        open();
        waitForElementPresent(By.id("date-picker"));
        checkLogsForErrors();
    }

}
