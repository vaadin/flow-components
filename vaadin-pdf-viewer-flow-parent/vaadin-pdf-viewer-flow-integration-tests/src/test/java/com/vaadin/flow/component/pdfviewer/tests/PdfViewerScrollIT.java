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

@TestPath("vaadin-pdf-viewer/scroll")
public class PdfViewerScrollIT extends AbstractComponentIT {

    private PdfViewerElement viewer;

    @Before
    public void init() {
        open();
        viewer = $(PdfViewerElement.class).single();
        waitUntil(driver -> !viewer.isLoading());
    }

    @Test
    public void scrollToLastPage_lastPageIsCurrent() {
        Assert.assertEquals(1, viewer.getPage());

        clickElementWithJs("scroll-to-last-page");

        waitUntil(driver -> viewer.getPage() == 6);
    }
}
