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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.pdfviewer.PdfViewer;
import com.vaadin.flow.component.pdfviewer.PdfViewerFeatureFlagProvider;
import com.vaadin.flow.dom.Element;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

class PdfViewerDownloadTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            PdfViewerFeatureFlagProvider.PDF_VIEWER_COMPONENT);

    private final PdfViewer viewer = new PdfViewer();

    @Test
    void defaultFileName() {
        Assertions.assertNull(viewer.getFileName());
        Assertions.assertFalse(viewer.getElement().hasProperty("fileName"));
    }

    @Test
    void setFileName_getFileName() {
        viewer.setFileName("report.pdf");
        Assertions.assertEquals("report.pdf",
                viewer.getElement().getProperty("fileName"));
        Assertions.assertEquals("report.pdf", viewer.getFileName());

        viewer.setFileName(null);
        Assertions.assertFalse(viewer.getElement().hasProperty("fileName"));
        Assertions.assertNull(viewer.getFileName());
    }

    @Test
    void download_downloadCalledOnClient() {
        ui.add(viewer);

        viewer.download();

        var calls = ui.dumpJsCalls(viewer.getElement(),
                Element.CallFunctionJs.class);
        Assertions.assertEquals(1, calls.size());
        Assertions.assertEquals(List.of("download"),
                calls.get(0).flattenArguments());
    }
}
