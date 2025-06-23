/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.tabs.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.Route;

/**
 * Test page for {@link TabSheet}.
 *
 * @author Vaadin Ltd.
 */
@Route("vaadin-tabs/tabsheet")
public class TabSheetPage extends Div {

    public TabSheetPage() {
        var tabsheet = new TabSheet();
        tabsheet.add("Tab one", new Span("Tab one content"));
        tabsheet.add("Tab two", new Span("Tab two content"));
        add(tabsheet);
    }

}
