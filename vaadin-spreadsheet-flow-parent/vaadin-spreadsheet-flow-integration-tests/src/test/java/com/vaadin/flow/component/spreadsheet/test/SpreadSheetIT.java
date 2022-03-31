package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.testbench.annotations.RunLocally;
import com.vaadin.testbench.parallel.Browser;
import com.vaadin.tests.AbstractParallelTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@RunLocally(Browser.CHROME)
public class SpreadSheetIT extends AbstractParallelTest {

    SpreadsheetElement spreadsheet;

    @Before
    public void init() {
        String url = getBaseURL().replace(super.getBaseURL(),
                super.getBaseURL() + "/vaadin-spreadsheet");
        getDriver().get(url);
    }

    @Test
    public void editColumnsAdded() {
        $("vaadin-button").id("createNewBtn").click();
        spreadsheet = $(SpreadsheetElement.class).waitForFirst();
        Assert.assertNotNull(spreadsheet);
    }
}
