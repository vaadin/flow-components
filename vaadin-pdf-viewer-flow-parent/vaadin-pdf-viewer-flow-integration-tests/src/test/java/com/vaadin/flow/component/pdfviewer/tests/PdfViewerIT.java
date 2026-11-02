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
    public void setUrlSrc_documentLoads() {
        clickElementWithJs("set-url-src");

        waitUntil(driver -> viewer.getPageCount() == 6);
    }

    @Test
    public void setDownloadHandlerSrc_documentLoadsWithFileNameFromResponse() {
        clickElementWithJs("set-handler-src");

        waitUntil(driver -> viewer.getPageCount() == 6);
        Assert.assertFalse(viewer.getSrc().endsWith(".pdf"));
        Assert.assertEquals("report.pdf", getHeaderTitle());
    }

    private String getHeaderTitle() {
        return (String) executeScript("""
                return arguments[0].shadowRoot
                    .querySelector('[part="title"]').textContent;
                """, viewer);
    }
}
