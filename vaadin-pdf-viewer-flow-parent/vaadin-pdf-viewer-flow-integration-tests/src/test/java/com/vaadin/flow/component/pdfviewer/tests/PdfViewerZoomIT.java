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
import org.openqa.selenium.Dimension;

import com.vaadin.flow.component.pdfviewer.testbench.PdfViewerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-pdf-viewer/zoom")
public class PdfViewerZoomIT extends AbstractComponentIT {

    // The width of the first page of sample.pdf at 100%: 595.92 PDF units of
    // 1/72 inch as CSS pixels of 1/96 inch
    private static final double PAGE_WIDTH_AT_100_PERCENT = 595.92 * 96 / 72;

    private PdfViewerElement viewer;

    @Before
    public void init() {
        open();
        viewer = $(PdfViewerElement.class).waitForSingle();
        waitUntil(driver -> viewer.getPageCount() > 0);
    }

    @Test
    public void setZoom_factor_pagesScaled() {
        clickElementWithJs("set-zoom-200");

        waitUntil(driver -> Math.abs(getFirstPageSize().getWidth()
                - 2 * PAGE_WIDTH_AT_100_PERCENT) <= 1);
        Assert.assertEquals("2", viewer.getZoom());
    }

    @Test
    public void setZoom_pageFit_pageFitsInView() {
        clickElementWithJs("set-zoom-200");
        waitUntil(driver -> getFirstPageSize().getWidth() > getContentSize()
                .getWidth());

        clickElementWithJs("set-zoom-page-fit");

        waitUntil(driver -> getFirstPageSize().getHeight() <= getContentSize()
                .getHeight());
        Assert.assertTrue(
                getFirstPageSize().getWidth() <= getContentSize().getWidth());
        Assert.assertEquals("page-fit", viewer.getZoom());
    }

    @Test
    public void zoomInOnClient_zoomChangeEventFired() {
        clickElementWithJs("set-zoom-100");
        waitUntil(driver -> "1.0 false".equals(getZoomInfo()));

        viewer.zoomIn();

        waitUntil(driver -> "1.25 true".equals(getZoomInfo()));
    }

    private String getZoomInfo() {
        return findElement(By.id("zoom-info")).getText();
    }

    private Dimension getFirstPageSize() {
        return viewer.$("div").withAttribute("part", "page").get(0).getSize();
    }

    private Dimension getContentSize() {
        return viewer.$("div").withAttribute("part", "content").single()
                .getSize();
    }
}
