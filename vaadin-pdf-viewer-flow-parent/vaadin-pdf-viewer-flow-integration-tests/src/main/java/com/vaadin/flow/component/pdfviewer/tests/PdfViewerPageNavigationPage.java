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
import com.vaadin.flow.component.pdfviewer.PdfViewer;
import com.vaadin.flow.router.Route;

@Route("vaadin-pdf-viewer/page-navigation")
public class PdfViewerPageNavigationPage extends Div {

    public PdfViewerPageNavigationPage() {
        var viewer = new PdfViewer("sample.pdf");
        viewer.setHeight("500px");

        var pageInfo = new Div();
        pageInfo.setId("page-info");
        viewer.addPageChangeListener(event -> pageInfo.setText(
                "Page " + event.getPage() + " of " + viewer.getPageCount()
                        + ", from client: " + event.isFromClient()));

        add(viewer, pageInfo);
    }
}
