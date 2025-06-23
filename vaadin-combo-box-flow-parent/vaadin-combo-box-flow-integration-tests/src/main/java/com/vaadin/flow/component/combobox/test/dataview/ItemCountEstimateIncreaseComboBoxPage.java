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

@Route("item-count-estimate-increase/:increase?([0-9]{1,9})")
public class ItemCountEstimateIncreaseComboBoxPage
        extends AbstractItemCountComboBoxPage {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        super.beforeEnter(event);
        event.getRouteParameters().get("increase").ifPresent(string -> {
            int size = Integer.parseInt(string);
            itemCountEstimateIncreaseInput.setValue(size);
        });
    }
}
