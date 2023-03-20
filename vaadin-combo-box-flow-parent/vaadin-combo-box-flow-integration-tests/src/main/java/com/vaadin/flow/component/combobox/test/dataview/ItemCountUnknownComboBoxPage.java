

package com.vaadin.flow.component.combobox.test.dataview;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;

@Route("item-count-unknown/:count?([0-9]{1,9})")
public class ItemCountUnknownComboBoxPage
        extends AbstractItemCountComboBoxPage {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        super.beforeEnter(event);
        switchToUndefinedSize();
        event.getRouteParameters().get("count")
                .ifPresent(string -> fetchCallbackSizeInput
                        .setValue(Integer.parseInt(string)));
    }
}
