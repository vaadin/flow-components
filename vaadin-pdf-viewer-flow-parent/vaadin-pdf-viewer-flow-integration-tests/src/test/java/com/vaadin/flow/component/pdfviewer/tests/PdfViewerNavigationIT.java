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

@TestPath("vaadin-pdf-viewer/navigation")
public class PdfViewerNavigationIT extends AbstractComponentIT {

    private PdfViewerElement viewer;

    @Before
    public void init() {
        open();
        viewer = $(PdfViewerElement.class).waitForSingle();
        waitUntil(driver -> viewer.getPageCount() == 6);
    }

    @Test
    public void setPage_viewerOnPage() {
        clickElementWithJs("set-page");

        waitUntil(driver -> viewer.getPage() == 3);
        Assert.assertEquals("3 from server", getText("page-info"));
    }

    @Test
    public void clickNextPage_pageAndPageCountSyncedToServer() {
        viewer.getNextPageButton().click();

        waitUntil(driver -> "2 from client".equals(getText("page-info")));
        clickElementWithJs("get-page");
        Assert.assertEquals("2/6", getText("server-page"));
    }

    @Test
    public void setSrcWithPage_documentOpensOnPage() {
        clickElementWithJs("set-src-with-page");

        waitUntil(driver -> viewer.getSrc().endsWith("?reload")
                && viewer.getPageCount() == 6);
        waitUntil(driver -> viewer.getPage() == 4);
    }

    private String getText(String id) {
        return findElement(By.id(id)).getText();
    }
}
