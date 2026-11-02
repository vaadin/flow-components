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
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;

@Route("vaadin-pdf-viewer")
public class PdfViewerPage extends Div {

    public PdfViewerPage() {
        var viewer = new PdfViewer();
        viewer.setHeight("500px");

        add(viewer);

        var setUrlSrc = new NativeButton("Set URL src",
                event -> viewer.setSrc("sample.pdf"));
        setUrlSrc.setId("set-url-src");

        var setHandlerSrc = new NativeButton("Set download handler src",
                event -> viewer.setSrc(
                        DownloadHandler.forServletResource("/sample.pdf")));
        setHandlerSrc.setId("set-handler-src");

        // The URL of this handler has no file name, so the viewer can only
        // get the name from the Content-Disposition header
        var setNamedHandlerSrc = new NativeButton(
                "Set download handler src with file name",
                event -> viewer.setSrc(DownloadHandler
                        .fromInputStream(downloadEvent -> new DownloadResponse(
                                ((VaadinServletRequest) downloadEvent
                                        .getRequest()).getServletContext()
                                        .getResourceAsStream("/sample.pdf"),
                                "report.pdf", "application/pdf", -1))));
        setNamedHandlerSrc.setId("set-named-handler-src");

        var setInvalidSrc = new NativeButton("Set invalid src",
                event -> viewer.setSrc("invalid.pdf"));
        setInvalidSrc.setId("set-invalid-src");

        var clearSrc = new NativeButton("Clear src",
                event -> viewer.setSrc((String) null));
        clearSrc.setId("clear-src");

        add(setUrlSrc, setHandlerSrc, setNamedHandlerSrc, setInvalidSrc,
                clearSrc);
    }
}
