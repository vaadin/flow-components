

package com.vaadin.flow.component.select.data;

import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.AbstractListDataViewListenerTest;
import com.vaadin.flow.data.provider.HasListDataView;

public class SelectListDataViewTest extends AbstractListDataViewListenerTest {

    /*
     * ListDataView implementation is tested in AbstractListDataViewTest. No
     * tests included here because SelectListDataView does not override any
     * methods or add any new ones.
     */

    @Override
    protected HasListDataView<String, ? extends AbstractListDataView<String>> getComponent() {
        return new Select<>();
    }
}
