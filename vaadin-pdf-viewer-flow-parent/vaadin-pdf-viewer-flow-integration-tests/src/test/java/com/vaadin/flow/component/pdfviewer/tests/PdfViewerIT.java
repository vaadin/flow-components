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

@TestPath("vaadin-pdf-viewer")
public class PdfViewerIT extends AbstractComponentIT {

    private PdfViewerElement viewer;

    @Before
    public void init() {
        open();
        viewer = $(PdfViewerElement.class).waitForSingle();
    }

    @Test
    public void noSrc_noPages() {
        Assert.assertNull(viewer.getSrc());
        Assert.assertEquals(0, viewer.getPageCount());
    }

    @Test
    public void setUrlSrc_documentLoads() {
        clickElementWithJs("set-url-src");

        waitUntil(driver -> viewer.getPageCount() == 6);
        Assert.assertEquals("6 pages, title: Multi-page fixture",
                findElement(By.id("load-info")).getText());
        Assert.assertFalse(viewer.isLoading());
        Assert.assertFalse(viewer.hasError());
    }

    @Test
    public void setDownloadHandlerSrc_documentLoads() {
        clickElementWithJs("set-handler-src");

        waitUntil(driver -> viewer.getPageCount() == 6);
        Assert.assertEquals("6 pages, title: Multi-page fixture",
                findElement(By.id("load-info")).getText());
    }

    @Test
    public void setUrlSrc_firstPageRendered() {
        clickElementWithJs("set-url-src");

        waitUntil(driver -> isFirstPageRendered());
    }

    @Test
    public void setInvalidSrc_errorEventFired() {
        clickElementWithJs("set-invalid-src");

        waitUntil(driver -> "INVALID"
                .equals(findElement(By.id("error-info")).getText()));
        Assert.assertTrue(viewer.hasError());
        Assert.assertEquals(0, viewer.getPageCount());
    }

    @Test
    public void clearSrc_documentUnloaded() {
        clickElementWithJs("set-url-src");
        waitUntil(driver -> viewer.getPageCount() == 6);

        clickElementWithJs("clear-src");

        waitUntil(driver -> viewer.getPageCount() == 0);
        Assert.assertNull(viewer.getSrc());
    }

    private boolean isFirstPageRendered() {
        return (Boolean) executeScript("""
                return !!arguments[0].shadowRoot
                    .querySelector('[part~="page"] canvas');
                """, viewer);
    }
}
