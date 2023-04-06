
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
