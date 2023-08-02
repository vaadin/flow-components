/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.grid;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;

/**
 * Event fired when Grid data provider changed.
 *
 * @param <T>
 *            the grid bean type
 *
 * @author Vaadin Ltd
 *
 * @see Grid#addDataProviderChangeListener(com.vaadin.flow.component.ComponentEventListener)
 *
 */
@DomEvent("data-provider-changed")
public class DataProviderChangeEvent<T> extends ComponentEvent<Grid<T>> {

    /**
     * Creates a new data provider change event.
     *
     * @param source
     *            the component that fired the event
     * @param fromClient
     *            whether the value change originated from the client
     */
    public DataProviderChangeEvent(Grid<T> source, boolean fromClient) {
        super(source, fromClient);
    }

}
