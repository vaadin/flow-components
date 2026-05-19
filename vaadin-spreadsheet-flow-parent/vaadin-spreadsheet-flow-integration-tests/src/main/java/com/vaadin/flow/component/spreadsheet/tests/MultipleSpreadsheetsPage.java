/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.framework.Action;
import com.vaadin.flow.component.spreadsheet.framework.Action.Handler;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Two independent spreadsheets on the same page, each with its own context
 * menu action and its own observable output. Verifies that the per-instance
 * overlay container introduced for {@code <vaadin-spreadsheet>} keeps the
 * two instances isolated.
 */
@Route("multiple-spreadsheets")
@PageTitle("Multiple spreadsheets")
public class MultipleSpreadsheetsPage extends VerticalLayout {

    public MultipleSpreadsheetsPage() {
        add(buildSpreadsheet("first", "Alpha"),
                buildSpreadsheet("second", "Beta"));
    }

    private VerticalLayout buildSpreadsheet(String id, String actionLabel) {
        Spreadsheet spreadsheet = new Spreadsheet();
        // setId sets the property, which the LitElement does not reflect to
        // the attribute; we need the attribute for TestBench element lookup.
        spreadsheet.getElement().setAttribute("id", id);
        spreadsheet.setHeight("250px");

        Div lastAction = new Div();
        lastAction.setId(id + "-last-action");

        Action action = new Action(actionLabel);
        spreadsheet.getContextMenuManager().addActionHandler(new Handler() {
            @Override
            public Action[] getActions(Object target, Object sender) {
                return new Action[] { action };
            }

            @Override
            public void handleAction(Action invoked, Object sender,
                    Object target) {
                if (invoked == action) {
                    lastAction.setText(invoked.getCaption());
                }
            }
        });

        VerticalLayout wrapper = new VerticalLayout(spreadsheet, lastAction);
        wrapper.setPadding(false);
        return wrapper;
    }
}
