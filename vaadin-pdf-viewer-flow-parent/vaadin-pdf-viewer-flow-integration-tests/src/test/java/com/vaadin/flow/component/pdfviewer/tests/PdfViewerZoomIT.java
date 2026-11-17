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
import com.vaadin.flow.component.pdfviewer.testbench.PdfViewerElement.ZoomMode;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-pdf-viewer/zoom")
public class PdfViewerZoomIT extends AbstractComponentIT {

    private PdfViewerElement viewer;

    @Before
    public void init() {
        open();
        viewer = $(PdfViewerElement.class).single();
        waitUntil(driver -> !viewer.isLoading());
    }

    @Test
    public void setZoom_getZoom() {
        viewer.setZoom(2);
        Assert.assertEquals("2", viewer.getZoom());

        viewer.setZoom(ZoomMode.PAGE_FIT);
        Assert.assertEquals("page-fit", viewer.getZoom());

        viewer.setZoom(ZoomMode.PAGE_WIDTH);
        Assert.assertEquals("page-width", viewer.getZoom());
    }

    @Test
    public void zoomIn_zoomOut() {
        viewer.setZoom(1);

        viewer.zoomIn();
        Assert.assertEquals("1.25", viewer.getZoom());

        viewer.zoomOut();
        Assert.assertEquals("1", viewer.getZoom());
    }
}
