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

@TestPath("vaadin-pdf-viewer/toolbar")
public class PdfViewerToolbarIT extends AbstractComponentIT {

    private PdfViewerElement viewer;

    @Before
    public void init() {
        open();
        viewer = $(PdfViewerElement.class).single();
    }

    @Test
    public void toggleToolbar_toolbarCollapsedChangeEventsFired() {
        viewer.toggleToolbar();
        Assert.assertTrue(viewer.isToolbarCollapsed());
        Assert.assertEquals("collapsed=true,fromClient=true;", getEvents());

        viewer.toggleToolbar();
        Assert.assertFalse(viewer.isToolbarCollapsed());
        Assert.assertEquals(
                "collapsed=true,fromClient=true;collapsed=false,fromClient=true;",
                getEvents());
    }

    private String getEvents() {
        return $("span").id("events").getText();
    }
}
