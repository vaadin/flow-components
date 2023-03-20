

package com.vaadin.flow.component.listbox.test.dataview;

import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.AbstractListDataViewListenerTest;
import com.vaadin.flow.data.provider.HasListDataView;

public class ListBoxListDataViewTest extends AbstractListDataViewListenerTest {

    /*
     * ListDataView implementation is tested in AbstractListDataViewTest. No
     * tests included here because ListBoxListDataView does not override any
     * methods or add any new ones.
     */

    @Override
    protected HasListDataView<String, ? extends AbstractListDataView<String>> getComponent() {
        return new ListBox<>();
    }
}
