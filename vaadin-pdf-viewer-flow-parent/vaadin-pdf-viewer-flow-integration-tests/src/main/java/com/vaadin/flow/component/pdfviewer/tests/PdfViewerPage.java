/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.pdfviewer.tests;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.pdfviewer.PdfViewer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.streams.DownloadEvent;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;

@Route("vaadin-pdf-viewer")
public class PdfViewerPage extends Div {

    // Download handlers run outside the session lock, so the release button
    // works while the slow handler waits
    private final CompletableFuture<Void> slowHandlerRelease = new CompletableFuture<>();

    public PdfViewerPage() {
        var viewer = new PdfViewer();
        viewer.setHeight("500px");

        add(viewer);

        // Shows the values the server has when the document load event fires
        var documentLoadListenerPageCount = new Span();
        documentLoadListenerPageCount
                .setId("document-load-listener-page-count");
        viewer.addDocumentLoadListener(event -> documentLoadListenerPageCount
                .setText(event.getPageCount() + "," + viewer.getPageCount()
                        + "," + viewer.pageCountSignal().peek()));
        add(documentLoadListenerPageCount);

        var setUrlSrc = new NativeButton("Set URL src",
                event -> viewer.setSrc("sample.pdf"));
        setUrlSrc.setId("set-url-src");

        // The URL of this handler has no file name, so the viewer can only
        // get the name from the Content-Disposition header
        var setHandlerSrc = new NativeButton("Set download handler src",
                event -> viewer.setSrc(DownloadHandler.fromInputStream(
                        downloadEvent -> createSampleResponse(downloadEvent,
                                "report.pdf"))));
        setHandlerSrc.setId("set-handler-src");

        var setInvalidSrc = new NativeButton("Set invalid src",
                event -> viewer.setSrc("invalid.pdf"));
        setInvalidSrc.setId("set-invalid-src");

        var setSlowHandlerSrc = new NativeButton(
                "Set slow download handler src", event -> viewer.setSrc(
                        DownloadHandler.fromInputStream(downloadEvent -> {
                            slowHandlerRelease.orTimeout(30, TimeUnit.SECONDS)
                                    .join();
                            return createSampleResponse(downloadEvent,
                                    "sample.pdf");
                        })));
        setSlowHandlerSrc.setId("set-slow-handler-src");

        var releaseSlowHandler = new NativeButton(
                "Release slow download handler",
                event -> slowHandlerRelease.complete(null));
        releaseSlowHandler.setId("release-slow-handler");

        add(setUrlSrc, setHandlerSrc, setInvalidSrc, setSlowHandlerSrc,
                releaseSlowHandler);
    }

    private static DownloadResponse createSampleResponse(
            DownloadEvent downloadEvent, String fileName) {
        var servletContext = ((VaadinServletRequest) downloadEvent.getRequest())
                .getServletContext();
        return new DownloadResponse(
                servletContext.getResourceAsStream("/sample.pdf"), fileName,
                "application/pdf", -1);
    }
}
