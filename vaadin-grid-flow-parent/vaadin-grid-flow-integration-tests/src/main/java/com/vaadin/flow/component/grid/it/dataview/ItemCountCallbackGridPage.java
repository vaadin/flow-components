/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.it.dataview;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/item-count-callback/:count?([0-9]{1,9})")
public class ItemCountCallbackGridPage extends AbstractItemCountGridPage {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().get("count")
                .ifPresent(string -> dataProviderSizeInput
                        .setValue(Integer.parseInt(string)));
        switchToDefinedSize();
    }
}
