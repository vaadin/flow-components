/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.ironlist.paging;

import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.StateNode;

import elemental.json.JsonArray;

/**
 * DataCommunicator implementation which disables the paging and does not allow
 * the user to set up the page for IronList.
 *
 * @param <T>
 *            item type
 */
public class PagelessDataCommunicator<T> extends DataCommunicator<T> {

    public PagelessDataCommunicator(DataGenerator<T> dataGenerator,
            ArrayUpdater arrayUpdater,
            SerializableConsumer<JsonArray> dataUpdater, StateNode stateNode) {
        super(dataGenerator, arrayUpdater, dataUpdater, stateNode);
        /*
         * Since IronList does not support pages, this forces the data
         * communicator to operate with items limit, not with the page number
         * and page size.
         */
        super.setPagingEnabled(false);
    }

    @Override
    public void setPageSize(int pageSize) {
        throw new UnsupportedOperationException(
                "IronList does not support paging");
    }

    @Override
    public void setPagingEnabled(boolean pagingEnabled) {
        throw new UnsupportedOperationException(
                "IronList does not support paging");
    }

    @Override
    public boolean isPagingEnabled() {
        return false;
    }
}
