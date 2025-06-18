/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.checkbox.dataview;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.AbstractListDataViewListenerTest;
import com.vaadin.flow.data.provider.HasListDataView;

public class CheckboxGroupListDataViewTest
        extends AbstractListDataViewListenerTest {

    /*
     * ListDataView implementation is tested in AbstractListDataViewTest. No
     * tests included here because CheckboxGroupListDataView does not override
     * any methods or add any new ones.
     */

    @Override
    protected HasListDataView<String, ? extends AbstractListDataView<String>> getComponent() {
        return new CheckboxGroup<>();
    }

}
