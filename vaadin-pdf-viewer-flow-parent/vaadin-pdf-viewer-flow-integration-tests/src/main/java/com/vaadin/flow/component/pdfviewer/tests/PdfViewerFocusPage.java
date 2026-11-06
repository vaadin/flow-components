/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.pdfviewer.tests;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.pdfviewer.PdfViewer;
import com.vaadin.flow.router.Route;

@Route("vaadin-pdf-viewer/focus")
public class PdfViewerFocusPage extends Div {

    private final List<String> events = new ArrayList<>();

    public PdfViewerFocusPage() {
        var viewer = new PdfViewer("sample.pdf");
        viewer.setHeight("500px");

        var eventLog = new Span();
        eventLog.setId("events");
        viewer.addFocusListener(event -> {
            events.add("focus " + event.isFromClient());
            eventLog.setText(String.join(", ", events));
        });
        viewer.addBlurListener(event -> {
            events.add("blur " + event.isFromClient());
            eventLog.setText(String.join(", ", events));
        });

        var focus = new NativeButton("Focus", event -> viewer.focus());
        focus.setId("focus");

        var blur = new NativeButton("Blur", event -> viewer.blur());
        blur.setId("blur");

        add(viewer, eventLog, focus, blur);
    }
}
