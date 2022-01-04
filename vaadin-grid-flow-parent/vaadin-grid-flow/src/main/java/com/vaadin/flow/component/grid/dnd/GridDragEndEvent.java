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
package com.vaadin.flow.component.grid.dnd;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.grid.Grid;

/**
 * Drag end event of {@link Grid} rows.
 *
 * @param <T>
 *            The Grid bean type.
 * @author Vaadin Ltd.
 * @see Grid#addDragEndListener(GridDragEndListener)
 */
@SuppressWarnings("serial")
@DomEvent("grid-dragend")
public class GridDragEndEvent<T> extends ComponentEvent<Grid<T>> {

    /**
     * Creates a grid drag end event.
     *
     * @param source
     *            Component that was dragged.
     * @param fromClient
     *            <code>true</code> if the event originated from the client
     *            side, <code>false</code> otherwise
     */
    public GridDragEndEvent(Grid<T> source, boolean fromClient) {
        super(source, true);
    }

}
