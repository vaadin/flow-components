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

@Route("vaadin-pdf-viewer/navigation")
public class PdfViewerNavigationPage extends Div {

    public PdfViewerNavigationPage() {
        var viewer = new PdfViewer("sample.pdf");
        viewer.setHeight("500px");

        var pageInfo = new Div();
        pageInfo.setId("page-info");
        viewer.addPageChangeListener(event -> pageInfo.setText(event.getPage()
                + (event.isFromClient() ? " from client" : " from server")));

        var loadInfo = new Div();
        loadInfo.setId("load-info");
        viewer.addDocumentLoadListener(
                event -> loadInfo.setText(event.getPageCount() + " pages"));

        add(viewer, pageInfo, loadInfo);

        var setPage = new NativeButton("Set page 3",
                event -> viewer.setPage(3));
        setPage.setId("set-page");

        var setSrcWithPage = new NativeButton("Set src and page 4", event -> {
            viewer.setSrc("sample.pdf?reload");
            viewer.setPage(4);
        });
        setSrcWithPage.setId("set-src-with-page");

        var showServerPage = new Div();
        showServerPage.setId("server-page");
        var getPage = new NativeButton("Get page", event -> showServerPage
                .setText(viewer.getPage() + "/" + viewer.getPageCount()));
        getPage.setId("get-page");

        add(setPage, setSrcWithPage, getPage, showServerPage);
    }
}
