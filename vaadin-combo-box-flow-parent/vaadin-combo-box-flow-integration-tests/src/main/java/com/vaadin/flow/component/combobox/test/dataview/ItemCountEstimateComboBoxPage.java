/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test.dataview;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;

@Route("item-count-estimate/:estimate?([0-9]{1,9})")
public class ItemCountEstimateComboBoxPage
        extends AbstractItemCountComboBoxPage {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        super.beforeEnter(event);
        event.getRouteParameters().get("estimate")
                .ifPresent(string -> itemCountEstimateInput
                        .setValue(Integer.parseInt(string)));
    }
}
