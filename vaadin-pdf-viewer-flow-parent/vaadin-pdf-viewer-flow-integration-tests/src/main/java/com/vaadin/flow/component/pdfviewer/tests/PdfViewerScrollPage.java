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

@Route("vaadin-pdf-viewer/scroll")
public class PdfViewerScrollPage extends Div {

    public PdfViewerScrollPage() {
        var viewer = new PdfViewer("sample.pdf");
        viewer.setHeight("500px");

        var scrollToLastPage = new NativeButton("Scroll to last page",
                event -> viewer.scrollToLastPage());
        scrollToLastPage.setId("scroll-to-last-page");

        add(viewer, scrollToLastPage);
    }
}
