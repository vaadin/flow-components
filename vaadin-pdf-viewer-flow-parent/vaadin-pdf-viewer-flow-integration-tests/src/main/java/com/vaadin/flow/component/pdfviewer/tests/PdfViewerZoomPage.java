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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.pdfviewer.PdfViewer;
import com.vaadin.flow.component.pdfviewer.Zoom;
import com.vaadin.flow.router.Route;

@Route("vaadin-pdf-viewer/zoom")
public class PdfViewerZoomPage extends Div {

    public PdfViewerZoomPage() {
        var viewer = new PdfViewer("sample.pdf");
        viewer.setHeight("500px");

        var zoomInfo = new Span();
        zoomInfo.setId("zoom-info");
        viewer.addZoomChangeListener(event -> zoomInfo
                .setText(event.getZoom() + " " + event.isFromClient()));

        var setZoom100 = new NativeButton("Zoom 100%",
                event -> viewer.setZoom(Zoom.of(1)));
        setZoom100.setId("set-zoom-100");

        var setZoom200 = new NativeButton("Zoom 200%",
                event -> viewer.setZoom(Zoom.of(2)));
        setZoom200.setId("set-zoom-200");

        var setZoomPageFit = new NativeButton("Zoom page fit",
                event -> viewer.setZoom(Zoom.PAGE_FIT));
        setZoomPageFit.setId("set-zoom-page-fit");

        add(viewer, zoomInfo, setZoom100, setZoom200, setZoomPageFit);
    }
}
