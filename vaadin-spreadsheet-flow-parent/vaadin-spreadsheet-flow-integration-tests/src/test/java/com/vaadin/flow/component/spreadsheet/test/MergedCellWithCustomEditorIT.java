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

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet/merged-cell-with-custom-editor")
public class MergedCellWithCustomEditorIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        setSpreadsheet($(SpreadsheetElement.class).waitForFirst());
    }

    @Test
    public void pageLoads_customEditorDisplayed() {
        var customEditor = $(TextFieldElement.class).waitForFirst();
        Assert.assertNotNull("Custom editor should be displayed initially",
                customEditor);
        Assert.assertTrue("Custom editor should be visible",
                customEditor.isDisplayed());
    }

    @Test
    public void refreshMergedCell_customEditorPreserved() {
        clickElementWithJs("refresh-merged-cell");
        var customEditor = $(TextFieldElement.class).waitForFirst();
        Assert.assertNotNull("Custom editor should still be displayed",
                customEditor);
        Assert.assertTrue("Custom editor should be visible",
                customEditor.isDisplayed());
    }

}
