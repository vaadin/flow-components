/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.pdfviewer.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.pdfviewer.PdfViewer;
import com.vaadin.flow.router.Route;

@Route("vaadin-pdf-viewer/download")
public class PdfViewerDownloadPage extends Div {

    public PdfViewerDownloadPage() {
        var viewer = new PdfViewer("sample.pdf");
        viewer.setHeight("500px");

        var download = new NativeButton("Download", event -> viewer.download());
        download.setId("download");

        var setFileName = new NativeButton("Set file name",
                event -> viewer.setFileName("report.pdf"));
        setFileName.setId("set-file-name");

        add(viewer, download, setFileName);
    }
}
