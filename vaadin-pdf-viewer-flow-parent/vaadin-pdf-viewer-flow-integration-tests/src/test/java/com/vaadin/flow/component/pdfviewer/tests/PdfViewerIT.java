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

        waitUntil(driver -> !viewer.isLoading());
        Assert.assertFalse(viewer.hasError());
        Assert.assertEquals(6, viewer.getPageCount());
        Assert.assertEquals("sample.pdf", viewer.getHeaderTitle());
    }

    @Test
    public void setInvalidSrc_hasError() {
        clickElementWithJs("set-invalid-src");

        waitUntil(driver -> !viewer.isLoading());
        Assert.assertTrue(viewer.hasError());
    }

    @Test
    public void setDownloadHandlerSrc_documentLoads() {
        clickElementWithJs("set-handler-src");

        waitUntil(driver -> !viewer.isLoading());
        Assert.assertFalse(viewer.hasError());
        Assert.assertEquals(6, viewer.getPageCount());
        Assert.assertEquals("report.pdf", viewer.getHeaderTitle());
    }

    @Test
    public void setSlowDownloadHandlerSrc_isLoadingUntilDocumentLoads() {
        Assert.assertFalse(viewer.isLoading());

        clickElementWithJs("set-slow-handler-src");
        waitUntil(driver -> viewer.isLoading());

        clickElementWithJs("release-slow-handler");
        waitUntil(driver -> !viewer.isLoading());
    }
}
