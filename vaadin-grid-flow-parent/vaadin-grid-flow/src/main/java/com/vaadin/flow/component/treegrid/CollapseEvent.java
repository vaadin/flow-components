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
package com.vaadin.flow.component.treegrid;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

public class CollapseEvent<T, C extends Component> extends ComponentEvent<C> {

    private final Collection<T> items;

    /**
     * CollapseEvent base constructor.
     *
     * @param source
     *            the source component
     * @param fromClient
     *            <code>true</code> if the event originated from the client
     *            side, <code>false</code> otherwise
     * @param items
     *            Collapsed items
     * @see ComponentEvent
     */
    public CollapseEvent(C source, boolean fromClient, Collection<T> items) {
        super(source, fromClient);
        this.items = Optional.ofNullable(items).orElse(Collections.emptyList());
    }

    public Collection<T> getItems() {
        return items;
    }
}
