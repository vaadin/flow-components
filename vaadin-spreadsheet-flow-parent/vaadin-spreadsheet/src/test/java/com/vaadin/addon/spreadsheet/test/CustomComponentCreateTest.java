package com.vaadin.addon.spreadsheet.test;

import org.junit.Test;

public class CustomComponentCreateTest extends Test1 {

    /**
     * Ticket #18546
     *
     * Note that this screenshot tests different themes so hence page must be
     * loaded between screenshot comparisons.
     */
    @Test
    public void CustomEditor_AddEditor_DisplayEditorImmediately()
            throws Exception {

        createNewSheet();
        loadServerFixture("SIMPLE_CUSTOM_EDITOR");

        compareScreen("customeditor_addcell_displayeditor");
    }

}
