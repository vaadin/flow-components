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
import com.vaadin.flow.router.Route;

@Route("vaadin-pdf-viewer/toolbar")
public class PdfViewerToolbarPage extends Div {

    public PdfViewerToolbarPage() {
        var viewer = new PdfViewer();
        viewer.setHeight("500px");
        add(viewer);

        var events = new Span();
        events.setId("events");
        viewer.addToolbarCollapsedChangeListener(event -> events.setText(
                events.getText() + "collapsed=" + event.isToolbarCollapsed()
                        + ",fromClient=" + event.isFromClient() + ";"));

        var collapseToolbar = new NativeButton("Collapse toolbar",
                event -> viewer.setToolbarCollapsed(true));
        collapseToolbar.setId("collapse-toolbar");

        var setHeaderTitle = new NativeButton("Set header title",
                event -> viewer.setHeaderTitle("Annual report"));
        setHeaderTitle.setId("set-header-title");

        var hideHeader = new NativeButton("Hide header",
                event -> viewer.setHeaderVisible(false));
        hideHeader.setId("hide-header");

        add(collapseToolbar, setHeaderTitle, hideHeader, events);
    }
}
