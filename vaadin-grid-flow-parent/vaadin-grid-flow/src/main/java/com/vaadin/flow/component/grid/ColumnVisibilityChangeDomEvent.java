/*
 * Copyright 2000-2026 Vaadin Ltd.
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
import com.vaadin.flow.component.EventData;

/**
 * Internal DOM event used by {@link Grid} to learn when the user shows or hides
 * a column on the client. The {@code column-visibility-changed} event is fired
 * by the {@code <vaadin-grid>} element when a column is toggled through its
 * column toggle menu and carries a reference to the toggled column element; its
 * {@code _flowId} property maps back to the server-side {@link Grid.Column}.
 *
 * <p>
 * For internal use only. Applications should listen to
 * {@link ColumnVisibilityChangedEvent} through
 * {@link Grid.Column#addVisibilityChangedListener} instead.
 *
 * @author Vaadin Ltd
 */
@DomEvent("column-visibility-changed")
public class ColumnVisibilityChangeDomEvent extends ComponentEvent<Grid<?>> {

    private final String columnInternalId;
    private final boolean hidden;

    public ColumnVisibilityChangeDomEvent(Grid<?> source, boolean fromClient,
            @EventData("event.detail.column._flowId") String columnInternalId,
            @EventData("event.detail.hidden") boolean hidden) {
        super(source, fromClient);
        this.columnInternalId = columnInternalId;
        this.hidden = hidden;
    }

    String getColumnInternalId() {
        return columnInternalId;
    }

    boolean isHidden() {
        return hidden;
    }
}
