/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.virtuallist.paging;

import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.StateNode;

import elemental.json.JsonArray;

/**
 * DataCommunicator implementation which disables the paging and does not allow
 * the user to set up the page for VirtualList.
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
         * Since VirtualList does not support pages, this forces the data
         * communicator to operate with items limit, not with the page number
         * and page size.
         */
        super.setPagingEnabled(false);
    }

    @Override
    public void setPageSize(int pageSize) {
        throw new UnsupportedOperationException(
                "VirtualList does not support paging");
    }

    @Override
    public void setPagingEnabled(boolean pagingEnabled) {
        throw new UnsupportedOperationException(
                "VirtualList does not support paging");
    }

    @Override
    public boolean isPagingEnabled() {
        return false;
    }
}
