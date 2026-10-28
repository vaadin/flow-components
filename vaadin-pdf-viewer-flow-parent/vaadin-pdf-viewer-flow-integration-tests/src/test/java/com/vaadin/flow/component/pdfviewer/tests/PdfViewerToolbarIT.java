/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.pdfviewer.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.pdfviewer.testbench.PdfViewerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-pdf-viewer/toolbar")
public class PdfViewerToolbarIT extends AbstractComponentIT {

    private PdfViewerElement viewer;

    @Before
    public void init() {
        open();
        viewer = $(PdfViewerElement.class).waitForSingle();
        waitUntil(driver -> viewer.getPageCount() == 6);
    }

    @Test
    public void addToToolbar_actionShownInToolbar() {
        Assert.assertTrue(isActionInToolbar());
    }

    @Test
    public void remove_actionRemoved() {
        clickElementWithJs("remove-action");

        waitUntil(driver -> !isElementPresent(By.id("toolbar-action")));
    }

    @Test
    public void setI18n_tooltipsUpdated() {
        clickElementWithJs("set-i18n");

        waitUntil(driver -> "Edellinen sivu"
                .equals(getTooltipText(viewer.getPreviousPageButton())));
        Assert.assertEquals("Seuraava sivu",
                getTooltipText(viewer.getNextPageButton()));
    }

    private boolean isActionInToolbar() {
        return (Boolean) executeScript("""
                const action = document.getElementById('toolbar-action');
                const toolbar = arguments[0].shadowRoot
                    .querySelector('[part="toolbar"]');
                return toolbar.contains(action.assignedSlot);
                """, viewer);
    }

    private String getTooltipText(Object button) {
        return (String) executeScript(
                "return arguments[0].querySelector('vaadin-tooltip').text;",
                button);
    }
}
