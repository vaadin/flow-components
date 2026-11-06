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

@TestPath("vaadin-pdf-viewer/focus")
public class PdfViewerFocusIT extends AbstractComponentIT {

    private PdfViewerElement viewer;

    @Before
    public void init() {
        open();
        viewer = $(PdfViewerElement.class).waitForSingle();
        waitUntil(driver -> viewer.getPageCount() > 0);
    }

    @Test
    public void focus_pageAreaFocused_focusEventFired() {
        clickElementWithJs("focus");

        waitUntil(driver -> viewer.isFocused());
        Assert.assertEquals(viewer, driver.switchTo().activeElement());
        waitUntil(driver -> "focus false".equals(getEvents()));
    }

    @Test
    public void focus_blur_focusRemoved_blurEventFired() {
        clickElementWithJs("focus");
        waitUntil(driver -> viewer.isFocused());

        clickElementWithJs("blur");

        waitUntil(driver -> !viewer.isFocused());
        waitUntil(driver -> "focus false, blur false".equals(getEvents()));
    }

    private String getEvents() {
        return findElement(By.id("events")).getText();
    }
}
