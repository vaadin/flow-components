/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import org.apache.poi.ss.util.CellReference;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.spreadsheet.PopupButton;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.dom.ShadowRoot;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * A {@code <vaadin-spreadsheet>} nested inside another element's shadow root.
 * The overlay container ({@code #spreadsheet-overlays}) then lives inside that
 * shadow tree, which a {@code document.head} stylesheet cannot reach. Selecting
 * a cell opens a popup-button overlay, letting the IT verify the overlay styles
 * still reach it. Reproduces the vaadin.com docs filtering example, which
 * embeds the spreadsheet as a web component (shadow DOM).
 */
@Route("spreadsheet-in-shadow-root")
@PageTitle("Spreadsheet in shadow root")
public class SpreadsheetInShadowRootPage extends Div {

    public SpreadsheetInShadowRootPage() {
        Div host = new Div();
        host.setId("shadow-host");
        host.setWidth("400px");
        host.setHeight("300px");
        ShadowRoot shadow = host.getElement().attachShadow();

        Spreadsheet spreadsheet = new Spreadsheet();
        spreadsheet.setHeight("250px");
        spreadsheet.addSelectionChangeListener(event -> {
            if (event.getAllSelectedCells().size() != 1) {
                return;
            }
            CellReference ref = event.getSelectedCellReference();
            CellReference newRef = new CellReference(
                    spreadsheet.getActiveSheet().getSheetName(), ref.getRow(),
                    ref.getCol(), false, false);
            PopupButton popupButton = new PopupButton();
            popupButton.setContent(new Span("Popup content"));
            spreadsheet.setPopup(newRef, popupButton);
            popupButton.openPopup();
        });

        shadow.appendChild(spreadsheet.getElement());
        add(host);
    }
}
