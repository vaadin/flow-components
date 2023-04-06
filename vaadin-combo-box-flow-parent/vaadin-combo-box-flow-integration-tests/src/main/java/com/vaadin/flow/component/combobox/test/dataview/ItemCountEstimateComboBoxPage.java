
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
