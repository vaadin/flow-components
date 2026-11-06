/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.pdfviewer.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.pdfviewer.testbench.PdfViewerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-pdf-viewer/download")
public class PdfViewerDownloadIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        var viewer = $(PdfViewerElement.class).waitForSingle();
        waitUntil(driver -> viewer.getPageCount() > 0);

        // The viewer downloads by clicking a link, so record the file names
        // of the clicked links instead of saving files
        executeScript("""
                window.downloadedFileNames = [];
                HTMLAnchorElement.prototype.click = function() {
                  window.downloadedFileNames.push(this.download);
                };
                """);
    }

    @Test
    public void download_documentDownloaded() {
        clickElementWithJs("download");

        waitUntil(driver -> !getDownloadedFileNames().isEmpty());
        Assert.assertEquals(List.of("sample.pdf"), getDownloadedFileNames());
    }

    @Test
    public void setFileName_download_fileNameUsed() {
        clickElementWithJs("set-file-name");
        clickElementWithJs("download");

        waitUntil(driver -> !getDownloadedFileNames().isEmpty());
        Assert.assertEquals(List.of("report.pdf"), getDownloadedFileNames());
    }

    @SuppressWarnings("unchecked")
    private List<String> getDownloadedFileNames() {
        return (List<String>) executeScript(
                "return window.downloadedFileNames");
    }
}
