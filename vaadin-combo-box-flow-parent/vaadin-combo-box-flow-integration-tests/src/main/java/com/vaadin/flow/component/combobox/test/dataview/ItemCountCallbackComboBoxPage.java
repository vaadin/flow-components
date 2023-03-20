

package com.vaadin.flow.component.combobox.test.dataview;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;

@Route("item-count-callback/:count?([0-9]{1,9})")
public class ItemCountCallbackComboBoxPage
        extends AbstractItemCountComboBoxPage {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        super.beforeEnter(event);
        event.getRouteParameters().get("count")
                .ifPresent(string -> dataProviderSizeInput
                        .setValue(Integer.parseInt(string)));
        switchToDefinedSize();
    }
}
