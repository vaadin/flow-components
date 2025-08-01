/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.select.data;

import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.HasListDataView;
import com.vaadin.tests.dataprovider.AbstractListDataViewListenerTest;

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
