
package com.vaadin.flow.component.radiobutton.dataview;

import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.AbstractListDataViewListenerTest;
import com.vaadin.flow.data.provider.HasListDataView;

public class RadioButtonGroupListDataViewTest
        extends AbstractListDataViewListenerTest {

    /*
     * ListDataView implementation is tested in AbstractListDataViewTest. No
     * tests included here because RadioButtonGroupListDataView does not
     * override any methods or add any new ones.
     */

    @Override
    protected HasListDataView<String, ? extends AbstractListDataView<String>> getComponent() {
        return new RadioButtonGroup<>();
    }
}
