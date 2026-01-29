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
        assertCustomEditorVisible();
    }

    @Test
    public void refreshMergedCell_customEditorPreserved() {
        clickElementWithJs("refresh-merged-cell");

        assertCustomEditorVisible();
    }

    @Test
    public void showEditorOnFocus_restoresCellValue() {
        clickElementWithJs("toggle-show-editor-on-focus");

        // Custom editor should be hidden
        assertCustomEditorHidden();

        // Cell should display its value
        Assert.assertEquals("Cell should display its value", "Merged cell",
                getMergedCellContent("B2"));

        // Focus cell to show editor
        clickCell("B2");

        // Custom editor should be visible
        assertCustomEditorVisible();

        // No cell content
        Assert.assertEquals("Cell content should be empty when editor is shown",
                "", getMergedCellContent("B2"));

        // Focus out to hide editor
        clickCell("A1");

        // Custom editor should be hidden
        assertCustomEditorHidden();

        // Cell should display its value again
        Assert.assertEquals(
                "Cell should display its value after editor is hidden",
                "Merged cell", getMergedCellContent("B2"));
    }

    private void assertCustomEditorVisible() {
        var customEditor = $(TextFieldElement.class).waitForFirst();
        Assert.assertNotNull("Custom editor should be displayed", customEditor);
        Assert.assertTrue("Custom editor should be visible",
                customEditor.isDisplayed());
    }

    private void assertCustomEditorHidden() {
        Assert.assertFalse("Custom editor should be hidden",
                $(TextFieldElement.class).exists());
    }
}
