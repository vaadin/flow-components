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
import com.vaadin.flow.component.pdfviewer.PdfViewerI18n;
import com.vaadin.flow.router.Route;

@Route("vaadin-pdf-viewer/toolbar")
public class PdfViewerToolbarPage extends Div {

    public PdfViewerToolbarPage() {
        var viewer = new PdfViewer("sample.pdf");
        viewer.setHeight("500px");

        var action = new NativeButton("Download");
        action.setId("toolbar-action");
        viewer.addToToolbar(action);

        add(viewer);

        var removeAction = new NativeButton("Remove action",
                event -> viewer.remove(action));
        removeAction.setId("remove-action");

        var setI18n = new NativeButton("Set i18n",
                event -> viewer.setI18n(
                        new PdfViewerI18n().setPreviousPage("Edellinen sivu")
                                .setNextPage("Seuraava sivu")));
        setI18n.setId("set-i18n");

        add(removeAction, setI18n);
    }
}
